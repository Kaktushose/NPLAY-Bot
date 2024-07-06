package com.github.kaktushose.nplaybot;

import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.rank.RankService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.user.GenericUserEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemberDatabaseSyncListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(MemberDatabaseSyncListener.class);
    private final RankService rankService;
    private final ItemService itemService;

    public MemberDatabaseSyncListener(Database database) {
        this.rankService = database.getRankService();
        this.itemService = database.getItemService();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        rankService.createUser(event.getMember());
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        rankService.createUser(event.getMember());
        rankService.updateRankRoles(event.getMember(), rankService.getUserInfo(event.getMember()).currentRank());
        itemService.updateItemRoles(event.getMember());
    }
}
