package de.kaktushose.nrtv.discord.frameworks.event;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;

public class RoleReward extends EventReward {


    public RoleReward(int bound, String name) {
        super(bound, name);
    }

    @Override
    public void onReward(BotUser botUser, Bot bot) {
        bot.addDiscordRole(bot.getGuild().getMemberById(botUser.getId()), bot.getGuild().getRoleById(827132547971678269L));
    }
}
