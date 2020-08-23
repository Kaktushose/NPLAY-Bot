package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.frameworks.command.HelpCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class GeneralHelpCommand implements HelpCommand {

    private final Bot bot;

    public GeneralHelpCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onCommand(Member executor, String[] args, TextChannel channel, Message message) {
        String p = "`" + bot.getPrefix();
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Hilfe")
                .setDescription("Folgende Commands stehen zur Verfügung. Siehe " + p + "hilfe <command> für Details`")
                .addField(p + "info (<@Member|id|\"Mem ber\">)`","Zeigt die Kontoinformationen zu einem User an", false)
                .addField(p + "rangliste`", "Zeigt eine Rangliste der User mit den meisten XP", false)
                .addField(p + "kaufen`", "Ermöglicht das Kaufen der Items, welche im <#648968903673905162> verfügbar sind", false)
                .addField(p + "tauschen`", "Tauscht einen Diamanten gegen 40 Münzen", false)
                .addField(p + "täglich`", "Aktiviert eine tägliche Direktnachricht mit allen Kontoinformationen.", false)
                .addField(p + "botinfo`", "Zeigt verschiedene Informationen zum Bot", false)
                .build()).queue();
    }

    @Override
    public void onInsufficientPermissions(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Fehler")
                .setColor(Color.RED)
                .setDescription("unzureichende Berechtigungen")
                .build()).queue();
    }

}
