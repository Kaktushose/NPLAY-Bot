package com.github.kaktushose.nplaybot.events.messages.delete;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.messages.delete.impl.ContestEntryDeletedEvent;
import com.github.kaktushose.nplaybot.events.reactions.starboard.StarboardPostDeleteEvent;
import com.github.kaktushose.nplaybot.features.events.contest.ContestEventService;
import com.github.kaktushose.nplaybot.features.starboard.StarboardService;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageDeleteListener extends ListenerAdapter {

    private final Bot bot;
    private final StarboardService starboardService;
    private final ContestEventService contestEventService;

    public MessageDeleteListener(Bot bot) {
        this.bot = bot;
        starboardService = bot.getDatabase().getStarboardService();
        contestEventService = bot.getDatabase().getContestEventService();
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        var dispatcher = bot.getEventDispatcher();

        if (starboardService.entryExists(event.getMessageIdLong())) {
            if (starboardService.isPosted(event.getMessageIdLong())) {
                dispatcher.dispatch(new StarboardPostDeleteEvent(event, bot));
            }
        }

        if (event.getChannel().getIdLong() == contestEventService.getContestEventChannel()) {
            dispatcher.dispatch(new ContestEntryDeletedEvent(event, bot));
        }
    }
}
