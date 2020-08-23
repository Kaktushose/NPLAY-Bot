package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.frameworks.command.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;


public class PingCommand extends Command {

    @Override
    @SubCommand(MEMBER_GROUP)
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage("pong").queue();
    }

    @SubCommand("pong")
    public void onPingPong(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage("\uD83C\uDFD3").queue();
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage("Lass uns Tischtennis spielen!").queue();
    }

}
