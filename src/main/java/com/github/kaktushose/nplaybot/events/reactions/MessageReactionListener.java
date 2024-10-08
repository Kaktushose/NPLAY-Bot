package com.github.kaktushose.nplaybot.events.reactions;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestReactionAddEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestReactionRemoveEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestVoteRemoveEvent;
import com.github.kaktushose.nplaybot.events.reactions.karma.KarmaDownvoteEvent;
import com.github.kaktushose.nplaybot.events.reactions.karma.KarmaUpvoteEvent;
import com.github.kaktushose.nplaybot.events.reactions.starboard.StarboardPostDeleteEvent;
import com.github.kaktushose.nplaybot.events.reactions.starboard.StarboardPostUpdateEvent;
import com.github.kaktushose.nplaybot.features.events.contest.ContestEventService;
import com.github.kaktushose.nplaybot.features.karma.KarmaService;
import com.github.kaktushose.nplaybot.features.rank.RankService;
import com.github.kaktushose.nplaybot.features.starboard.StarboardService;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageReactionListener extends ListenerAdapter {

    private final Bot bot;
    private final PermissionsService permissionsService;
    private final ContestEventService eventService;
    private final StarboardService starboardService;
    private final KarmaService karmaService;
    private final RankService rankService;

    public MessageReactionListener(Bot bot) {
        this.bot = bot;
        permissionsService = bot.getDatabase().getPermissionsService();
        eventService = bot.getDatabase().getContestEventService();
        starboardService = bot.getDatabase().getStarboardService();
        karmaService = bot.getDatabase().getKarmaService();
        rankService = bot.getDatabase().getRankService();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            handleContestVote(event);
        }

        if (event.getReaction().getEmoji().equals(Emoji.fromUnicode("⭐"))) {
            handleStarboardVote(event);
        }

        if (rankService.isValidChannel(event.getChannel())) {
            if (karmaService.getValidUpvoteEmojis().contains(event.getEmoji())) {
                handleKarmaUpvote(event);
            }
            if (karmaService.getValidDownvoteEmojis().contains(event.getEmoji())) {
                handleKarmaDownvote(event);
            }
        }
    }

    private void handleContestVote(MessageReactionAddEvent event) {
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }
        bot.getEventDispatcher().dispatch(new ContestReactionAddEvent(event, bot));
    }

    private void handleStarboardVote(MessageReactionAddEvent event) {
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
        }

        event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
            if (!permissionsService.hasPermissions(message.getAuthor(), Set.of(BotPermissions.USER))) {
                return;
            }

            AtomicInteger count = new AtomicInteger(0);

            Optional.ofNullable(message.getReaction(Emoji.fromFormatted("⭐"))).ifPresent(
                    it -> count.set(it.getCount())
            );

            long messageId = message.getIdLong();
            if (!starboardService.entryExists(messageId)) {
                starboardService.createEntry(messageId);
            }
            if (count.get() < starboardService.getThreshold()) {
                return;
            }
            bot.getEventDispatcher().dispatch(new StarboardPostUpdateEvent(event, bot, count.get(), message));
        });
    }

    private void handleKarmaUpvote(MessageReactionAddEvent event) {
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }
        // prevent Erich abuse
        if (event.getUser().getIdLong() == event.getMessageAuthorIdLong()) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        event.retrieveMessage().queue(message -> {
            if (!permissionsService.hasUserPermissions(message.getMember())) {
                return;
            }
            bot.getEventDispatcher().dispatch(new KarmaUpvoteEvent(event, bot, message));
        });
    }

    private void handleKarmaDownvote(MessageReactionAddEvent event) {
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }
        // prevent Erich abuse
        if (event.getUser().getIdLong() == event.getMessageAuthorIdLong()) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        event.retrieveMessage().queue(message -> {
            if (!permissionsService.hasUserPermissions(message.getMember())) {
                return;
            }
            bot.getEventDispatcher().dispatch(new KarmaDownvoteEvent(event, bot, message));
        });
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        var dispatcher = bot.getEventDispatcher();

        if (event.getUser().isBot()) {
            return;
        }
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }

        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            if (event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
                dispatcher.dispatch(new ContestVoteRemoveEvent(event, bot));
            }
        }

        if (event.getReaction().getEmoji().equals(Emoji.fromUnicode("⭐"))) {
            event.getChannel().retrieveMessageById(event.getMessageIdLong()).queue(message -> {
                if (!permissionsService.hasUserPermissions(message.getMember())) {
                    return;
                }

                AtomicInteger count = new AtomicInteger(0);
                Optional.ofNullable(message.getReaction(Emoji.fromFormatted("⭐"))).ifPresent(
                        it -> count.set(it.getCount())
                );

                long messageId = message.getIdLong();
                if (count.get() == 0) {
                    if (!starboardService.entryExists(messageId)) {
                        return;
                    }
                    if (starboardService.isPosted(messageId)) {
                        dispatcher.dispatch(new StarboardPostDeleteEvent(event, bot));
                    }
                    return;
                }

                if (!starboardService.entryExists(messageId)) {
                    starboardService.createEntry(messageId);
                }
                if (count.get() < starboardService.getThreshold()) {
                    if (starboardService.isPosted(messageId)) {
                        dispatcher.dispatch(new StarboardPostDeleteEvent(event, bot));
                    }
                } else {
                    dispatcher.dispatch(new StarboardPostUpdateEvent(event, bot, count.get(), message));
                }
            });
        }

        if (rankService.isValidChannel(event.getChannel())) {
            event.retrieveMessage().queue(message -> {
                if (!permissionsService.hasUserPermissions(message.getMember())) {
                    return;
                }

                if (event.getUser().getIdLong() == message.getAuthor().getIdLong()) {
                    return;
                }
                if (karmaService.getValidUpvoteEmojis().contains(event.getEmoji())) {
                    dispatcher.dispatch(new KarmaDownvoteEvent(event, bot, message));
                    return;
                }
                if (karmaService.getValidDownvoteEmojis().contains(event.getEmoji())) {
                    dispatcher.dispatch(new KarmaUpvoteEvent(event, bot, message));
                }
            });
        }
    }

    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        var dispatcher = bot.getEventDispatcher();

        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            if (event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
                dispatcher.dispatch(new ContestReactionRemoveEvent(event, bot));
            }
        }

        if (event.getReaction().getEmoji().equals(Emoji.fromUnicode("⭐"))) {
            if (starboardService.entryExists(event.getMessageIdLong())) {
                if (starboardService.isPosted(event.getMessageIdLong())) {
                    dispatcher.dispatch(new StarboardPostDeleteEvent(event, bot));
                }
            }

        }
    }

    @Override
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {
        var dispatcher = bot.getEventDispatcher();

        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            dispatcher.dispatch(new ContestReactionRemoveEvent(event, bot));
        }

        if (starboardService.entryExists(event.getMessageIdLong())) {
            if (starboardService.isPosted(event.getMessageIdLong())) {
                dispatcher.dispatch(new StarboardPostDeleteEvent(event, bot));
            }
        }
    }
}
