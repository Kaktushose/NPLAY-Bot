package de.kaktushose.nrtv.discord.core.bot.listeners;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class JoinLeaveListener extends ListenerAdapter {

    private Bot bot;

    public JoinLeaveListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if (event.getUser().isBot()) return;
        BotUser botUser = new BotUser(event.getMember().getIdLong());
        botUser.setCoins(100);
        bot.getGuild().addRoleToMember(event.getMember(), bot.getRoleByLevel(0)).queue();
        bot.getDatabase().addBotUser(botUser);
    }

    @Override
    public void onGuildMemberLeave(@Nonnull GuildMemberLeaveEvent event) {
        bot.getDatabase().removeBotUser(event.getMember().getIdLong());
    }
}
