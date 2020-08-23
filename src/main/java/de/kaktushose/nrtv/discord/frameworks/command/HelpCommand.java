package de.kaktushose.nrtv.discord.frameworks.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public interface HelpCommand {

    void onCommand(Member executor, String[] args, TextChannel channel, Message message);
    void onInsufficientPermissions(Member executor, TextChannel channel);

}
