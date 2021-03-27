package de.kaktushose.nrtv.discord.frameworks.level;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ItemCheck {

    private final Bot bot;
    private final ScheduledExecutorService executorService;

    public ItemCheck(Bot bot) {
        this.bot = bot;
        executorService = Executors.newScheduledThreadPool(1);
    }

    public void check(Guild guild) {
        guild.getMembers().forEach(member -> {
            BotUser botUser = bot.getDatabase().getBotUser(member.getIdLong());
            botUser.getItemStack().forEach((itemType, item) -> {
                long buyTime = botUser.getBuyTime(itemType);
                if (item.getRemainingTimeAsLong(buyTime) < 0) {
                    item.onItemExpiration(bot, member);
                    return;
                }
                if (item.isExpiring(buyTime)) {
                    schedule(member, item, item.getRemainingTimeAsLong(buyTime));
                }
            });
        });
    }

    private void schedule(Member member, Item item, long delay) {
        executorService.schedule(() -> item.onItemExpiration(bot, member), delay, TimeUnit.MILLISECONDS);
    }

}
