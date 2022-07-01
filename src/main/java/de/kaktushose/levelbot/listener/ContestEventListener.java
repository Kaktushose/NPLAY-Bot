package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.database.services.EventService;
import de.kaktushose.levelbot.database.services.SettingsService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEmojiEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ContestEventListener extends ListenerAdapter {

    private final SettingsService settingsService;
    private final EventService eventService;

    public ContestEventListener(SettingsService settingsService, EventService eventService) {
        this.settingsService = settingsService;
        this.eventService = eventService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        long guildId = event.getGuild().getIdLong();

        if (event.getAuthor().isBot()) {
            return;
        }

        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }

        if (eventService.voteCountExists(event.getAuthor().getIdLong())) {
            event.getMessage().delete().queue();
            return;
        }

        event.getMessage().addReaction(Emoji.fromFormatted(settingsService.getEventEmote(guildId))).queue();
        eventService.createVoteCount(event.getMessage().getIdLong(), event.getAuthor().getIdLong());
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        long guildId = event.getGuild().getIdLong();

        if (event.getUser().isBot()) {
            return;
        }
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }
        if (event.getEmoji().getName().equals(settingsService.getEventEmote(guildId))) {
            eventService.increaseVoteCount(event.getMessageIdLong());
        }
    }

    // single reaction removed
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        long guildId = event.getGuild().getIdLong();
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }
        if (event.getEmoji().getName().equals(settingsService.getEventEmote(guildId))) {
            eventService.decreaseVoteCount(event.getMessageIdLong());
        }
    }

    // all reactions of a emote removed
    @Override
    public void onMessageReactionRemoveEmoji(@NotNull MessageReactionRemoveEmojiEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        long guildId = event.getGuild().getIdLong();
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }
        if (event.getEmoji().getName().equals(settingsService.getEventEmote(guildId))) {
            removeEntry(event);
        }
    }

    // all reactions removed
    @Override
    public void onMessageReactionRemoveAll(@NotNull MessageReactionRemoveAllEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(event.getGuild().getIdLong())) {
            return;
        }
        removeEntry(event);
    }

    // message deleted
    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(event.getGuild().getIdLong())) {
            return;
        }
        removeEntry(event);
    }

    private void removeEntry(GenericMessageEvent event) {
        eventService.deleteVoteCount(event.getMessageIdLong());
    }
}
