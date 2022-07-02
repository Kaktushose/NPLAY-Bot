package de.kaktushose.levelbot.listener;

import de.kaktushose.levelbot.Levelbot;
import de.kaktushose.levelbot.account.data.BotUser;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class JoinLeaveListener extends ListenerAdapter {

    private final Levelbot levelbot;

    public JoinLeaveListener(Levelbot levelbot) {
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        BotUser botUser = levelbot.getUserService().createUserIfAbsent(event.getMember().getIdLong());
        levelbot.getUserService().addCoins(botUser.getUserId(), 100);
        levelbot.getTaskScheduler().addSingleTask(
                () -> levelbot.addRankRole(event.getMember().getIdLong(), 1),
                20,
                TimeUnit.MINUTES
        );
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        levelbot.getUserService().deleteUser(event.getUser().getIdLong());
        levelbot.getBoosterService().changeNitroBoosterStatus(event.getUser().getIdLong(), false);
    }
}
