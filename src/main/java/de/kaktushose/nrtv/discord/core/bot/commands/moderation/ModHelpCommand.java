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

@Permissions(PermissionLevel.MODERATOR)
public class ModHelpCommand extends Command {

    private Bot bot;

    public ModHelpCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        String p = "`" + bot.getPrefix();
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Hilfe Moderation")
                .setDescription("Folgende Commands stehen zur Verfügung. Siehe " + p + "hilfe <command> für Details`")
                .addField(p + "add <coins|xp|diamonds> <@Member|@Rolle|all> <amount>`", "Fügt einer Usergruppe die angegebene Anzahl an XP, Münzen oder Diamanten hinzu", false)
                .addField(p + "set <coins|xp|diamonds> <@Member|@Rolle|all> <amount>`", "Setzt die XP, Münzen oder Diamanten einer Usergruppe auf den angegeben Wert", false)
                .addField(p + "delete <amount>`", "Löscht die angegebene Anzahl an Nachrichten", false)
                .addField(p + "mute <@Member>`", "Mutet einen User beim Bot", false)
                .addField(p + "remove <@Member>`", "Entfernt ein Item aus dem Besitz eines Users", false)
                .addField(p + "kaufen <@Member|@Rolle|all>`", "Ermöglicht das Kaufen von Items für eine Usergruppe. Gleiche Menüführung wie beim privaten Kauf", false)
                .addField(p + "setperms <@Member|@Rolle|all> <level>`", "Setzt das Berechtigungslevel für eine Usergruppe auf das angegebene Level", false)
                .addField(p + "stop`", "Fährt den Bot herunter", false)
                .build()).queue();
    }
}