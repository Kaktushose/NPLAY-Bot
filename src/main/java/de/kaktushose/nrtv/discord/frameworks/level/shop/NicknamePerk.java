package de.kaktushose.nrtv.discord.frameworks.level.shop;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NicknamePerk extends Item {

    public NicknamePerk(int price, String name, String description, long duration, int type) {
        super(price, name, description, duration, type, ItemType.NICKNAME);
    }

    @Override
    public boolean validateTransaction(BotUser botUser) {
        if (botUser.hasItem(itemType)) {
            error = "Du besitzt dieses Item bereits!";
            return false;
        }
        if (botUser.getCoins() < price) {
            error = "Du hast nicht genügend Münzen!";
            return false;
        }
        return true;
    }

    @Override
    public void buy(Bot bot, Member member) {
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        botUser.getItemStack().put(itemType, this);
        botUser.setNickNameBuyTime(System.currentTimeMillis());
        botUser.setCoins(botUser.getCoins() - price);
        bot.addRole(member, Bot.Roles.NICKNAME);
        bot.getDatabase().setBotUser(botUser);
    }

    @Override
    public void onItemExpiration(Bot bot, Member member) {
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        if (System.currentTimeMillis() - botUser.getBoosterBuyTime() >= duration) {
            botUser.getItemStack().remove(ItemType.NICKNAME);
            bot.removeRole(member, Bot.Roles.NICKNAME);
            botUser.setNickNameBuyTime(0);
            bot.getDatabase().setBotUser(botUser);

            member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle("Item abgelaufen!")
                    .setDescription("Dein " + name + " ist abgelaufen!")
                    .addField("Gekauft am:", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(botUser.getNickNameBuyTime())), false)
                    .build()).queue(msg -> {}, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER)));

        }
    }

    @Override
    public String getSuccessMessage() {
        return "Du bestitzt den " + name + "!";
    }
}
