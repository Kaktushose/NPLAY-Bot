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

public class PremiumRole extends Item {

    public PremiumRole(int price, String name, String description, long duration, int type) {
        super(price, name, description, duration, type, ItemType.PREMIUM);
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
        botUser.setCoins(botUser.getCoins() - price);
        botUser.setPremiumBuyTime(System.currentTimeMillis());
        bot.getDatabase().setBotUser(botUser);
        bot.addRole(member, Bot.Roles.PREMIUM);
    }

    @Override
    public void onItemExpiration(Bot bot, Member member) {
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        if (System.currentTimeMillis() - botUser.getBoosterBuyTime() >= duration) {
            botUser.getItemStack().remove(ItemType.PREMIUM);
            bot.removeRole(member, Bot.Roles.PREMIUM);
            botUser.setPremiumBuyTime(0);
            bot.getDatabase().setBotUser(botUser);

            member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle("Item abgelaufen!")
                    .setDescription("Dein " + name + " ist abgelaufen!")
                    .addField("Gekauft am:", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(botUser.getPremiumBuyTime())), false)
                    .build()).queue(msg -> {}, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER)));

        }
    }

    @Override
    public String getSuccessMessage() {
        return "Du besitzt jetzt " + name + "!";
    }


}
