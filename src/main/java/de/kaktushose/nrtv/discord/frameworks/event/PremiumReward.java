package de.kaktushose.nrtv.discord.frameworks.event;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;
import de.kaktushose.nrtv.discord.frameworks.level.shop.PremiumRole;
import net.dv8tion.jda.api.entities.Member;

public class PremiumReward extends EventReward {


    public PremiumReward(int bound, String name) {
        super(bound, name);
    }

    @Override
    public void onReward(BotUser botUser, Bot bot) {
        Item item = botUser.getItemStack().get(ItemType.PREMIUM);
        if (item != null) {
            if (item.getType() == 3) {
                return;
            }
        }
        bot.addUpItem(botUser, bot.getDatabase().getItemType(1, ItemType.PREMIUM));
        Member member = bot.getGuild().getMemberById(botUser.getId());
        bot.addRole(member, Bot.Roles.PREMIUM);
        bot.getDatabase().setBotUser(botUser);
    }
}
