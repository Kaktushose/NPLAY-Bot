package de.kaktushose.nrtv.discord.core.bot.commands.moderation;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.command.Permissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

@Permissions(PermissionLevel.ADMIN)
public class StopCommand extends Command {

    private Bot bot;

    public StopCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("Bot wurde heruntergefahren")
                .build()).complete();
        System.exit(0);
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "stop`")
                .addField("Beschreibung:", "Fährt den Bot herunter.\n" +
                        "Nur im Notfall benutzen!", false)
                .addField("Berechtigungslevel", PermissionLevel.ADMIN.name(), false)
                .addField("seit Version", "2.1.0", false)
                .build()).queue();
    }
}
