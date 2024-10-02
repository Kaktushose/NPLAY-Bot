package com.github.kaktushose.nplaybot.features.events.contest;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.BotEvent;
import com.github.kaktushose.nplaybot.events.messages.delete.impl.ContestEntryDeletedEvent;
import com.github.kaktushose.nplaybot.events.messages.receive.impl.ContestMessageEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestReactionAddEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestReactionRemoveEvent;
import com.github.kaktushose.nplaybot.events.reactions.contest.ContestVoteRemoveEvent;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContestListener {

    private static final Logger log = LoggerFactory.getLogger(ContestListener.class);
    private final ContestEventService eventService;

    public ContestListener(Bot bot) {
        this.eventService = bot.getDatabase().getContestEventService();
    }

    @BotEvent
    public void onContestMessage(ContestMessageEvent event) {
        if (eventService.createContestEntry(event.getMessage())) {
            event.getMessage().addReaction(Emoji.fromFormatted(eventService.getVoteEmoji())).queue();
        } else {
            log.debug("User {} already has a contest entry, deleting new message", event.getMember());
            event.getMessage().delete().queue();
        }
    }

    @BotEvent
    public void onContestEntryDeleted(ContestEntryDeletedEvent event) {
        eventService.setVoteCount(event.getMessageId(), 0);
        log.debug("Contest entry {} got deleted, setting vote count to 0", event.getMessageId());
    }

    @BotEvent
    public void onVoteAdd(ContestReactionAddEvent event) {
        if (event.getUserId() == event.getMessageAuthorId()) {
            log.debug("Removing self vote from contest entry");
            event.withMessage(message -> message.removeReaction(event.getEmoji(), event.getUser()).queue());
            return;
        }
        if (!event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
            log.debug("Removing invalid emoji from contest entry");
            event.withMessage(message -> message.removeReaction(event.getEmoji(), event.getUser()).queue());
            return;
        }
        eventService.increaseVoteCount(event.getMessageId(), event.getUserId());
    }


    @BotEvent
    public void onContestVoteRemove(ContestVoteRemoveEvent event) {
        eventService.decreaseVoteCount(event.getMessageId(), event.getUserId());
    }

    @BotEvent
    public void onContestReactionRemove(ContestReactionRemoveEvent event) {
        log.warn("Detected removal of all vote emojis. Adding initial emoji again");
        event.withMessage(message -> message.addReaction(Emoji.fromFormatted(eventService.getVoteEmoji())).queue());
    }
}
