package de.kaktushose.nrtv.discord.frameworks.level.shop;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import net.dv8tion.jda.api.entities.Member;

public class DiamondExchanger {

    private int price;
    private String name, description, error;

    public DiamondExchanger() {
        price = 1;
        name =  "Diamanten-Tauscher";
        description = "Tausche Deine überflüssigen Diamanten wieder in Münzen ein - und hol Dir unsere tollen Items!";
   }


    public boolean validateTransaction(BotUser botUser) {
        if (botUser.getDiamonds() < 1) {
            error = "Du hast nicht genügend Diamanten!";
            return false;
        }
        return true;
    }

    public void buy(Bot bot, Member member) {
        BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
        botUser.setDiamonds(botUser.getDiamonds() - 1);
        botUser.setCoins(botUser.getCoins() + 40);
        bot.getDatabase().setBotUser(botUser);
    }

    public String getSuccessMessage() {
        return "Du hast einen Diamanten gegen 40 Münzen getauscht!";
    }

    public String getErrorMessage() {
        return error;
    }

}
