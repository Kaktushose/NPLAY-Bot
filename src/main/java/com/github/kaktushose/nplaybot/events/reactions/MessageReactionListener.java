package com.github.kaktushose.nplaybot.events.reactions;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.EventDispatcher;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestReactionAddEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestReactionRemoveEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestVoteRemoveEvent;
import com.github.kaktushose.nplaybot.features.events.contest.ContestEventService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageReactionListener extends ListenerAdapter {

    private final Bot bot;
    private final PermissionsService permissionsService;
    private final ContestEventService eventService;
    private final EventDispatcher dispatcher;

    public MessageReactionListener(Bot bot) {
        this.bot = bot;
        permissionsService = bot.getDatabase().getPermissionsService();
        dispatcher = bot.getEventDispatcher();
        eventService = bot.getDatabase().getContestEventService();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            dispatcher.dispatch(new ContestReactionAddEvent(event, bot));
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
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
    }

    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            if (event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
                dispatcher.dispatch(new ContestReactionRemoveEvent(event, bot));
            }
        }
    }

    @Override
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {
        if (event.getChannel().getIdLong() == eventService.getContestEventChannel()) {
            dispatcher.dispatch(new ContestReactionRemoveEvent(event, bot));
        }
    }
}
