package de.kaktushose.nrtv.discord.core.bot.commands.moderation;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Permissions(PermissionLevel.ADMIN)
public class SetPermissionCommand extends Command {

    private Bot bot;

    public SetPermissionCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Syntaxfehler")
                .addField("Syntax",
                        "`" + bot.getPrefix() + "setperms <@Member|@Rolle|all> <level>`",
                        true).build()).queue();
    }

    @SubCommand({MEMBER_GROUP, NUMBER})
    public void onSetPermissions(Member executor, Arguments args, TextChannel channel, Message message) {
        int level = args.getAsInteger(1);

        if (level < 0 || level > 4) {
            onCommand(executor, args, channel, message);
            return;
        }
		//TODO rechte nicht höher vergeben als die eigenen
        List<Member> updatedMember = new ArrayList<>();
        args.getAsMemberList(0).forEach(member -> {
            updatedMember.add(member);
            BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
            botUser.setPermissionLevel(level);
            bot.getDatabase().setBotUser(botUser);
        });

        String member;
        if (updatedMember.size() == 1) {
            member = updatedMember.get(0).getEffectiveName();
        } else if (updatedMember.size() == 2) {
            member = updatedMember.get(0).getEffectiveName() + " und " + updatedMember.get(1).getEffectiveName();
        } else {
            member = updatedMember.get(0).getEffectiveName() + " und " + (updatedMember.size() - 1)  + " weiteren Membern";
        }

        channel.sendMessage(new EmbedBuilder()
                .setTitle("Erfolg!")
                .setDescription("Das Berechtigungslevel von " + member + " wurde auf " + level + " gesetzt")
                .setColor(Color.GREEN)
                .build()).queue();
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "setperms <@Member|@Rolle|all> <level>`")
                .addField("Beschreibung:", "Setzt das Berechtigungslevel für eine Usergruppe auf das angegebene Level.\n" +
                        "Der Wert darf dabei nicht das eigene Level überschreiten.\n", false)
                .addField("verfügbare Level", "0: Muted\n1: Member\n2: Moderator\n3: Admin\n4: Bot Owner", false)
                .addField("Berechtigungslevel", PermissionLevel.ADMIN.name(), false)
                .addField("seit Version", "2.0.0", false)
                .build()).queue();    }
}
