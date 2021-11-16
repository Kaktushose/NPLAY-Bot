package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.model.BotUser;
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
        BotUser botUser = levelbot.getUserService().createUserIfAbsent(event.getMember().getIdLong());
        levelbot.getUserService().addCoins(botUser.getUserId(), 100);
        levelbot.addRankRole(event.getMember().getIdLong(), 1);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        levelbot.getUserService().deleteUser(event.getUser().getIdLong());
    }
}
