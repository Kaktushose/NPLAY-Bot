package de.kaktushose.nrtv.discord.core.bot.commands.user;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.Arguments;
import de.kaktushose.nrtv.discord.frameworks.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

public class GiftCommand extends Command {

    private Bot bot;

    public GiftCommand(Bot bot) {
        this.bot = bot;
    }

    @Override
    protected void onCommand(Member executor, Arguments args, TextChannel channel, Message message) {
        BotUser botUser = bot.getDatabase().getBotUser(executor.getIdLong());
        if (!bot.getDatabase().getGiftedUserIds().contains(executor.getIdLong())) {
            botUser.setXp(botUser.getXp() + 20);
            botUser.setCoins(botUser.getCoins() + 20);
            botUser.setDiamonds(botUser.getDiamonds() + 3);
            bot.getDatabase().setBotUser(botUser);
            bot.getDatabase().addGiftedUserId(executor.getIdLong());
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("nordrheintvplay wünscht frohe Festtage! :christmas_tree:")
                    .setColor(Color.GREEN)
                    .setDescription("Du hast folgendes geschenkt bekommen:\n" +
                            "20 XP :star2:\n" +
                            "20 Münzen :moneybag:\n" +
                            "3 Diamanten :gem:");
            channel.sendMessage(embedBuilder.build()).queue();
        } else {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setDescription(String.format("Du hast dein Geschenk bereits erhalten, %s!\nFrohe Festtage!", executor.getAsMention()));
            channel.sendMessage(embedBuilder.build()).queue();        }
    }
}
