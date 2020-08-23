package de.kaktushose.nrtv.discord.core.bot.commands.moderation;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.frameworks.command.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Permissions(PermissionLevel.MODERATOR)
public class BulkDeleteCommand extends Command {

    private Bot bot;

    public BulkDeleteCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Syntaxfehler")
                .setDescription("Fehler: Bitte eine Zahl zwischen 2 und 100 angeben!")
                .addField("Syntax",
                        "`" + bot.getPrefix() + "delete <amount>`",
                        true).build()).queue();
    }

    @SubCommand(NUMBER)
    public void onDelete(Member executor, Arguments args, TextChannel channel, Message message) {
        int amount = args.getAsInteger(0);
        if (amount < 2 || amount > 100) {
            channel.sendMessage(new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle("Syntaxfehler")
                    .setDescription("Fehler: Bitte eine Zahl zwischen 2 und 100 angeben!")
                    .addField("Syntax",
                            "`" + bot.getPrefix() + "delete <amount>`",
                            true).build()).queue();
            return;
        }

        message.delete().queue();
        channel.getHistory().retrievePast(amount).queue(messages ->
                messages.forEach(msg -> msg.delete().queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))));

        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setDescription(amount + " Nachrichten wurden gelöscht").build()).queue(msg -> msg.delete().queueAfter(2, TimeUnit.SECONDS));
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" +  bot.getPrefix() + "delete <amount>`")
                .addField("Beschreibung:", "Löscht die angegebene Anzahl an Nachrichten.\n" +
                        "Es sind nur Zahlen von `2` bis `100` erlaubt.", false)
                .addField("Berechtigungslevel", PermissionLevel.MODERATOR.name(), false)
                .addField("seit Version", "2.0.0", false)
                .build()).queue();
    }
}
