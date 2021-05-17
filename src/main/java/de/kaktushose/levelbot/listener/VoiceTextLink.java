package de.kaktushose.levelbot.listener;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

public class VoiceTextLink extends ListenerAdapter {

    private final TextChannel textChannel;

    public VoiceTextLink(TextChannel textChannel) {
        this.textChannel = textChannel;
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        textChannel.getManager().putPermissionOverride(event.getMember(), EnumSet.of(Permission.VIEW_CHANNEL), null).queue();
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        textChannel.getManager().removePermissionOverride(event.getMember()).queue();
    }
}
