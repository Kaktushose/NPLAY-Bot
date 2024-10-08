package com.github.kaktushose.nplaybot.events.contest;

import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContestListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ContestListener.class);
    private final ContestEventService eventService;
    private final PermissionsService permissionsService;

    public ContestListener(Database database) {
        this.eventService = database.getContestEventService();
        this.permissionsService = database.getPermissionsService();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        if (event.getChannel().getIdLong() != eventService.getContestEventChannel()) {
            return;
        }
        if (event.getAuthor().isBot()) {
            return;
        }

        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getMessage().delete().queue();
            return;
        }

        if (eventService.createContestEntry(event.getMessage())) {
            event.getMessage().addReaction(Emoji.fromFormatted(eventService.getVoteEmoji())).queue();
        } else {
            log.info("User {} already has a contest entry, deleting new message", event.getAuthor());
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (event.getChannel().getIdLong() != eventService.getContestEventChannel()) {
            return;
        }
        eventService.setVoteCount(event.getMessageIdLong(), 0);
        log.info("Contest entry {} got deleted, setting vote count to 0", event.getMessageId());
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getChannel().getIdLong() != eventService.getContestEventChannel()) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        if (event.getUser().getIdLong() == event.getMessageAuthorIdLong()) {
            log.debug("Removing self vote from contest entry");
            event.retrieveMessage().flatMap(message -> message.removeReaction(event.getEmoji(), event.getUser())).queue();
            return;
        }
        if (!event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
            log.debug("Removing invalid emoji from contest entry");
            event.retrieveMessage().flatMap(message -> message.removeReaction(event.getEmoji(), event.getUser())).queue();
            return;
        }
        eventService.increaseVoteCount(event.getMessageIdLong(), event.getUserIdLong());
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getChannel().getIdLong() != eventService.getContestEventChannel()) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }
        if (!event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
            return;
        }
        eventService.decreaseVoteCount(event.getMessageIdLong(), event.getUserIdLong());
    }

    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        if (event.getChannel().getIdLong() != eventService.getContestEventChannel()) {
            return;
        }
        if (!event.getEmoji().equals(Emoji.fromFormatted(eventService.getVoteEmoji()))) {
            return;
        }
        log.warn("Detected removal of all vote emojis. Adding initial emoji again");
        event.getChannel().retrieveMessageById(event.getMessageId()).flatMap(message ->
                message.addReaction(Emoji.fromFormatted(eventService.getVoteEmoji()))
        ).queue();

    }

    @Override
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {
        if (event.getChannel().getIdLong() != eventService.getContestEventChannel()) {
            return;
        }
        log.warn("Detected removal of all vote emojis. Adding initial emoji again");
        event.getChannel().retrieveMessageById(event.getMessageId()).flatMap(message ->
                message.addReaction(Emoji.fromFormatted(eventService.getVoteEmoji()))
        ).queue();
    }
}
