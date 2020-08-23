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

@Permissions(PermissionLevel.MODERATOR)
public class SetCommand extends Command {

    private Bot bot;

    public SetCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("Syntaxfehler!")
                .addField("Syntax:", "`" + bot.getPrefix() + "set <coins|xp|diamonds> <@Member|@Rolle|all> <amount>`", false)
                .setColor(Color.ORANGE)
                .build()).queue();

    }

    @SubCommand({"coins", MEMBER_GROUP, NUMBER})
    public void onSetCoins(Member executor, Arguments args, TextChannel channel, Message message) {
        List<Member> updatedMember = new ArrayList<>();
        args.getAsMemberList(1).forEach(member -> {
            updatedMember.add(member);
            BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
            botUser.setCoins(args.getAsInteger(2));
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
                .setDescription("Die Münzen von " + member + " wurden auf " + args.getAsInteger(2) + " gesetzt")
                .setColor(Color.GREEN)
                .build()).queue();
    }

    @SubCommand({"xp", MEMBER_GROUP, NUMBER})
    public void onSetXp(Member executor, Arguments args, TextChannel channel, Message message) {
        List<Member> updatedMember = new ArrayList<>();
        args.getAsMemberList(1).forEach(member -> {
            updatedMember.add(member);
            BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
            botUser.setXp(args.getAsInteger(2));
            bot.getDatabase().setBotUser(botUser);
        });

        String member;
        if (updatedMember.size() == 1) {
            member = updatedMember.get(0).getEffectiveName();
        } else if (updatedMember.size() == 2) {
            member = updatedMember.get(0).getEffectiveName() + " und " + updatedMember.get(1).getEffectiveName();
        } else {
            member = updatedMember.get(0).getEffectiveName() + " und " + (updatedMember.size() - 1)  + " weiteren Member";
        }

        channel.sendMessage(new EmbedBuilder()
                .setTitle("Erfolg!")
                .setDescription("Die XP von " + member + " wurden auf " + args.getAsInteger(2) + " gesetzt")
                .setColor(Color.GREEN)
                .build()).queue();
    }

    @SubCommand({"diamonds", MEMBER_GROUP, NUMBER})
    public void onSetDiamonds(Member executor, Arguments args, TextChannel channel, Message message) {
        List<Member> updatedMember = new ArrayList<>();
        args.getAsMemberList(1).forEach(member -> {
            updatedMember.add(member);
            BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
            botUser.setDiamonds(args.getAsInteger(2));
            bot.getDatabase().setBotUser(botUser);
        });

        String member;
        if (updatedMember.size() == 1) {
            member = updatedMember.get(0).getEffectiveName();
        } else if (updatedMember.size() == 2) {
            member = updatedMember.get(0).getEffectiveName() + " und " + updatedMember.get(1).getEffectiveName();
        } else {
            member = updatedMember.get(0).getEffectiveName() + " und " + (updatedMember.size() - 1)  + " weiteren Member";
        }

        channel.sendMessage(new EmbedBuilder()
                .setTitle("Erfolg!")
                .setDescription("Die Diamanten von " + member + " wurden auf " + args.getAsInteger(2) + " gesetzt")
                .setColor(Color.GREEN)
                .build()).queue();
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "set <coins|xp|diamonds> <@Member|@Rolle|all> <amount>`")
                .addField("Beschreibung:", "Setzt die XP, Münzen oder Diamanten einer Usergruppe auf den angegeben Wert.\n" +
                        "Es sind nur Zahlen von `-2147483648` bis `2147483647` erlaubt.", false)
                .addField("Berechtigungslevel", PermissionLevel.MODERATOR.name(), false)
                .addField("seit Version", "2.0.0", false)
                .build()).queue();    }
}
