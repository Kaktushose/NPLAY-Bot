package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;

public class DailyCommand extends Command {

    private final Bot bot;

    public DailyCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        BotUser botUser = bot.getDatabase().getBotUser(executor.getIdLong());
        EmbedBuilder builder = new EmbedBuilder();
        if (botUser.isDaily()) {
            builder.setDescription("Die tägliche Kontoinformation wurde erfolgreich deaktiviert").setColor(Color.GREEN);
        } else {
            builder.setDescription("Die tägliche Kontoinformation wurde erfolgreich aktiviert").setColor(Color.GREEN);
        }
        botUser.setDaily(!botUser.isDaily());
        bot.getDatabase().setBotUser(botUser);
        executor.getUser().openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(builder.build()))
                .flatMap(msg -> message.delete())
                .queue(null, new ErrorHandler()
                        .handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                            builder.setColor(Color.ORANGE)
                                    .addField("Warnung", "Der Bot kann dir keine Direktnachrichten senden. Bitte überprüfe deine Einstellungen", false);
                            channel.sendMessage(builder.build()).queue();
                        }));
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "täglich`")
                .addField("Beschreibung:", "Aktiviert eine tägliche Direktnachricht mit allen Kontoinformationen.", false)
                .addField("Berechtigungslevel", PermissionLevel.MEMBER.name(), false)
                .addField("seit Version", "2.2.0", false)
                .build()).queue();
    }
}
