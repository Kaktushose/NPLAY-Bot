package com.github.kaktushose.nplaybot.karma;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.rank.RankService;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class KarmaListener extends ListenerAdapter {

    private final KarmaService karmaService;
    private final RankService rankService;
    private final EmbedCache embedCache;

    public KarmaListener(Database database, EmbedCache embedCache) {
        karmaService = database.getKarmaService();
        rankService = database.getRankService();
        this.embedCache = embedCache;
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }

        if (karmaService.getValidUpvoteEmojis().contains(event.getEmoji())) {
            // prevent Erich abuse
            if (event.getUser().getIdLong() == event.getMessageAuthorIdLong()) {
                event.getReaction().removeReaction(event.getUser()).queue();
                return;
            }

            int oldKarma = rankService.getUserInfo(UserSnowflake.fromId(event.getMessageAuthorIdLong())).karma();
            int newKarma = karmaService.onKarmaVoteAdd(event.getUser(), UserSnowflake.fromId(event.getMessageAuthorIdLong()), true);

            event.retrieveMessage().queue(message -> karmaService.onKarmaIncrease(oldKarma, newKarma, message.getMember(), embedCache));
        } else if (karmaService.getValidDownvoteEmojis().contains(event.getEmoji())) {
            if (event.getUser().getIdLong() == event.getMessageAuthorIdLong()) {
                event.getReaction().removeReaction(event.getUser()).queue();
                return;
            }

            int oldKarma = rankService.getUserInfo(UserSnowflake.fromId(event.getMessageAuthorIdLong())).karma();
            int newKarma = karmaService.onKarmaVoteRemove(event.getUser(), UserSnowflake.fromId(event.getMessageAuthorIdLong()), true);

            event.retrieveMessage().queue(message -> karmaService.onKarmaIncrease(oldKarma, newKarma, message.getMember(), embedCache));
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        event.retrieveMessage().queue(message -> {
            if (event.getUser().getIdLong() == message.getAuthor().getIdLong()) {
                return;
            }
            if (karmaService.getValidUpvoteEmojis().contains(event.getEmoji())) {
                int oldKarma = rankService.getUserInfo(message.getAuthor()).karma();
                int newKarma = karmaService.onKarmaVoteRemove(event.getUser(), message.getAuthor(), false);
                karmaService.onKarmaDecrease(oldKarma, newKarma, message.getMember(), embedCache);
            } else if (karmaService.getValidDownvoteEmojis().contains(event.getEmoji())) {
                int oldKarma = rankService.getUserInfo(message.getAuthor()).karma();
                int newKarma = karmaService.onKarmaVoteAdd(event.getUser(), message.getAuthor(), false);
                karmaService.onKarmaDecrease(oldKarma, newKarma, message.getMember(), embedCache);
            }
        });
    }
}
