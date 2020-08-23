package de.kaktushose.nrtv.discord.frameworks.event;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.level.shop.PremiumRole;

public class PremiumReward extends EventReward {


    public PremiumReward(int bound, String name) {
        super(bound, name);
    }

    @Override
    public void onReward(BotUser botUser, Bot bot) {
        bot.addUpItem(botUser, new PremiumRole(0, ":star: PREMIUM basic",
                "Erhalte mit dieser besonderen Rolle satte 13 exklusive Vorteile auf unserem Server!",
                2592000000L,
                1));
    }
}
