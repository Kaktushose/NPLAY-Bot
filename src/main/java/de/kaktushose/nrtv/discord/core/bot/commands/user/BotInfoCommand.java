package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class BotInfoCommand extends Command {

    private Bot bot;

    public BotInfoCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Informationen zum Bot")
                .addField("Prefix", "`" + bot.getPrefix() + "`", false)
                .addField("Version", bot.getVersion(), false)
                .addField("Credits:", "Konzept und Idee: " + bot.getJda().getUserById(307973135746072578L).getAsMention() + "\n" +
                        "Programmierung: " + bot.getJda().getUserById(393843637437464588L).getAsMention() + "\n" +
                        "Logo und Testing: " + bot.getJda().getUserById(487320784759685130L).getAsMention(), false)
                .build()).queue();
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" +  bot.getPrefix() + "botinfo`")
                .addField("Beschreibung:", "Zeigt verschiedene Informationen zum Bot.", false)
                .addField("Berechtigungslevel", PermissionLevel.MEMBER.name(), false)
                .addField("seit Version", "2.1.0", false)
                .build()).queue();
    }
}
