package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class GiftCommand extends Command {

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage("Das Geschenk ist erst ab dem 24.12 verfügbar!").queue();
    }
}
