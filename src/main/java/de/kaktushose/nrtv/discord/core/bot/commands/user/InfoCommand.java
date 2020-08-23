package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.command.SubCommand;
import de.kaktushose.nrtv.discord.frameworks.event.EventPoint;
import de.kaktushose.nrtv.discord.frameworks.event.EventType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;


public class InfoCommand extends Command {

    private final Bot bot;

    public InfoCommand(Bot bot) {
        this.bot = bot;
    }

    public void sendDM(Member target, PrivateChannel channel) {
        EmbedBuilder builder = getInfoEmbed(bot.getDatabase().getBotUser(target.getIdLong()), target, true);
        channel.sendMessage(builder.build()).queue();
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        sendMemberInfo(executor, channel);
    }

    @SubCommand(MEMBER_MENTION)
    public void onMemberInfo(Member executor, Arguments args, TextChannel channel, Message message) {
        sendMemberInfo(args.getAsMember(0), channel);
    }

    private void sendMemberInfo(Member member, TextChannel channel) {
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        if (!botUser.exists()) {
            channel.sendMessage(new EmbedBuilder().setTitle("Fehler")
                    .appendDescription("Konnte den angegebenen Nutzer nicht finden! " +
                            "Wenn du denkst, dass es sich um einen Fehler handelt, kontaktiere bitte "
                            + channel.getGuild().getMemberById(393843637437464588L).getAsMention())
                    .addField("Hinweis", "Bots sind vom Level-System ausgeschlossen", false)
                    .setColor(Color.RED).build()).queue();
            return;
        }
        channel.sendMessage(getInfoEmbed(botUser, member, false).build()).queue();
    }

    private EmbedBuilder getInfoEmbed(BotUser botUser, Member member, boolean isDM) {
        int xpFlow, coinsFlow, diamondFlow, xpToNext;

        xpFlow = botUser.getXp() - botUser.getStartXp();
        coinsFlow = botUser.getCoins() - botUser.getStartCoins();
        diamondFlow = botUser.getDiamonds() - botUser.getStartDiamonds();
        String rank = "\uD83C\uDF9A️ ",
                nextRank = "\uD83C\uDFAF ";
        if (botUser.getLevel() == 9) {
            nextRank = "N/A";
        }
        else {
            xpToNext = bot.getDatabase().getXpBounds().get(botUser.getLevel()) - botUser.getXp();
            nextRank += !isDM ? bot.getRoleByLevel(botUser.getLevel() + 1).getAsMention() + " (noch " + xpToNext + " XP)" : bot.getRoleByLevel(botUser.getLevel() + 1).getName() + " (noch " + xpToNext + " XP)";
        }
        rank += !isDM ? bot.getRoleByLevel(botUser.getLevel()).getAsMention() : bot.getRoleByLevel(botUser.getLevel()).getName();

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("ℹ Kontoinformation für ")
                .setDescription(member.getAsMention())
                .setThumbnail(member.getUser().getAvatarUrl())
                .setColor(bot.getColor(botUser.getLevel()))
                .addField("Stufe:", rank, false)
                .addField("Nächste Stufe:", nextRank, false)
                .addField("", "__Währungen__:", false)
                .addField("XP: ", "\uD83C\uDF1F " + botUser.getXp(), true)
                .addField("Münzen: ", "\uD83D\uDCB0 " + botUser.getCoins(), true)
                .addField("Diamanten: ", "\uD83D\uDC8E " + botUser.getDiamonds(), true);

        if (bot.eventIsPresent()) {
            EventPoint eventPoint = bot.getDatabase().getEventPoint(EventType.SUMMER);
            builder.addField(eventPoint.getName() + ":", eventPoint.getEmote() + " " + botUser.getEventPoints(), false);
            if (botUser.getEventPoints() >= 10) {
                builder.addField("Belohnung Sammelaktion:", ":sunny: Eventrolle SOMMER 2020", false);
            } else {
                builder.addField("Belohnung Sammelaktion:", ":x: noch keine", false);
            }
        }

        builder.addField("", "__Statistiken__:", false)
                .addField("Ressourcen-Zuwachs:", String.format(":star2: %d | :moneybag: %d | :gem: %d", xpFlow, coinsFlow, diamondFlow), false)
                .addField("Gezählte Nachrichten:", ":chart_with_upwards_trend: " + botUser.getMessages(), false);

        builder.addField("", "__Items__:", false);

        if (botUser.getItemStack().values().isEmpty()) {
            builder.addField("Keine Items in Besitz", "", false);
            builder.setFooter("Mit dem Befehl !kaufen kannst du dir Items kaufen.");
        } else {
            botUser.getItemStack().values().forEach(item ->
                    builder.addField(
                            item.getName(),
                            item.getRemainingTimeAsDate(botUser.getBuyTime(item.getItemType())) + " verbleibend",
                            false));
        }
        return builder;
    }

    @Override
    protected void onHelp(Member executor, TextChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Command Details")
                .setDescription("Syntax: `" + bot.getPrefix() + "info (<@Member|id|\"Mem ber\">)`")
                .addField("Beschreibung:", "Zeigt die Kontoinformationen zu einem User an.\n" +
                        "Bots sind vom Levelsystem ausgeschlossen.", false)
                .addField("Berechtigungslevel", PermissionLevel.MEMBER.name(), false)
                .addField("seit Version", "2.0.0", false)
                .build()).queue();
    }

}
