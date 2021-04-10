package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener extends ListenerAdapter {

    private final Levelbot levelbot;

    public JoinLeaveListener(Levelbot levelbot) {
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        levelbot.getUserService().createIfAbsent(event.getMember().getIdLong());
        levelbot.addRankRole(event.getMember().getIdLong(), 1);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        levelbot.getUserService().delete(event.getUser().getIdLong());
    }
}
