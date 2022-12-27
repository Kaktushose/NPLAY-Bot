package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.database.services.EventService;
import de.kaktushose.levelbot.database.services.SettingsService;
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
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
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
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

        event.getMessage().addReaction(settingsService.getEventEmote(guildId)).queue();
        eventService.createVoteCount(event.getMessage().getIdLong(), event.getAuthor().getIdLong());
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (event.getUser().isBot()) {
            return;
        }
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }
        if (eventService.isSelfUser(event.getMessageIdLong(), event.getUserIdLong())) {
            return;
        }
        if (event.getReactionEmote().getName().equals(settingsService.getEventEmote(guildId))) {
            eventService.increaseVoteCount(event.getMessageIdLong());
        }
    }

    // single reaction removed
    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }
        if (event.getReactionEmote().getName().equals(settingsService.getEventEmote(guildId))) {
            eventService.decreaseVoteCount(event.getMessageIdLong());
        }
    }

    // all reactions of a emote removed
    @Override
    public void onGuildMessageReactionRemoveEmote(@NotNull GuildMessageReactionRemoveEmoteEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(guildId)) {
            return;
        }
        if (event.getReactionEmote().getName().equals(settingsService.getEventEmote(guildId))) {
            removeEntry(event);
        }
    }

    // all reactions removed
    @Override
    public void onGuildMessageReactionRemoveAll(@NotNull GuildMessageReactionRemoveAllEvent event) {
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(event.getGuild().getIdLong())) {
            return;
        }
        removeEntry(event);
    }

    // message deleted
    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        if (event.getChannel().getIdLong() != settingsService.getEventChannelId(event.getGuild().getIdLong())) {
            return;
        }
        removeEntry(event);
    }

    private void removeEntry(GenericGuildMessageEvent event) {
        eventService.deleteVoteCount(event.getMessageIdLong());
    }
}
