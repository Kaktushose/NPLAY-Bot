package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener extends ListenerAdapter {

    private final RankService rankService;
    private final PermissionsService permissionsService;

    public JoinLeaveListener(Database database) {
        this.rankService = database.getRankService();
        this.permissionsService = database.getPermissionsService();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        rankService.createUser(event.getUser());
        rankService.updateRankRoles(event.getMember(), rankService.getUserInfo(event.getMember()).currentRank());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }
        rankService.removeUser(event.getUser());
    }
}
