package com.github.kaktushose.nplaybot;

import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MemberDatabaseSyncListener extends ListenerAdapter {

    private final Database database;

    public MemberDatabaseSyncListener(Database database) {
        this.database = database;
    }

    @Override
    public void onGenericGuildMember(@NotNull GenericGuildMemberEvent event) {
        database.getRankService().createUser(event.getMember());
    }
}
