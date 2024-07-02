package com.github.kaktushose.nplaybot.rank;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener extends ListenerAdapter {

    private final RankService rankService;

    public JoinLeaveListener(RankService rankService) {
        this.rankService = rankService;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        rankService.createUser(event.getUser());
        rankService.updateRankRoles(event.getMember(), rankService.getUserInfo(event.getMember()).currentRank());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        rankService.removeUser(event.getUser());
    }
}
