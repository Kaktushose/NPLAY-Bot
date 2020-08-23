package de.kaktushose.nrtv.discord.frameworks.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public abstract class Command {

    protected abstract void onCommand(Member executor, Arguments args, TextChannel channel, Message message);

    protected void onInsufficientPermissions(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Fehler")
                .setColor(Color.RED)
                .setDescription("unzureichende Berechtigungen")
                .build()).queue();
    }

    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage("No specific help found").queue();
    }

    @Deprecated
    protected final String ROLE_MENTION = "@Rolle";
    protected final String MEMBER_MENTION = "@Member";
    protected final String CHANNEL_MENTION = "#Channel";
    protected final String NUMBER = "Zahl";
    protected final String MEMBER_GROUP = "<@Member|@Rolle|all>";

}

