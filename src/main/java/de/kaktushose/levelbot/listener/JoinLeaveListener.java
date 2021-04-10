package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.database.service.UserService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener extends ListenerAdapter {

    private final UserService userService;

    public JoinLeaveListener(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        userService.createIfAbsent(event.getMember().getIdLong());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        userService.delete(event.getUser().getIdLong());
    }
}
