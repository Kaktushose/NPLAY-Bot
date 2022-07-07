package de.kaktushose.levelbot.leveling.listener;

import de.kaktushose.levelbot.bot.Levelbot;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class LeaveListener extends ListenerAdapter {

    private final Levelbot levelbot;

    public LeaveListener(Levelbot levelbot) {
        this.levelbot = levelbot;
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        levelbot.getUserService().deleteUser(event.getUser().getIdLong());
        levelbot.getBoosterService().changeNitroBoosterStatus(event.getUser().getIdLong(), false);
    }
}
