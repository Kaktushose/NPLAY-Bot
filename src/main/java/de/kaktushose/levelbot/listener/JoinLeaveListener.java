package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener extends ListenerAdapter {

    private final Database database;

    public JoinLeaveListener(Database database) {
        this.database = database;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        database.getUsers().save(new BotUser(event.getUser().getIdLong()));
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        database.getUsers().deleteById(event.getUser().getIdLong());
    }
}
