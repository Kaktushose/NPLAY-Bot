package com.github.kaktushose.nplaybot.items;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ItemExpirationTask {

    private static final Logger log = LoggerFactory.getLogger(ItemExpirationTask.class);
    public static final int PLAY_ACTIVITY_KARMA_THRESHOLD = 30;
    private final ScheduledExecutorService executor;

    public ItemExpirationTask() {
        executor = Executors.newScheduledThreadPool(4, runnable -> new Thread(runnable, "ItemExpiration"));
    }

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS)
    public void onCheckItems(Bot bot) {
        var itemService = bot.getDatabase().getItemService();
        for (var expiring : itemService.getExpiringTransactions()) {
            executor.schedule(() -> {
                var transaction = itemService.getTransactionById(expiring.transactionId());
                log.info("Starting removal for transaction {}", transaction);
                if (transaction.expiresAt() > System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)) {
                    log.info("Removal got invalidated. Transaction probably got prolonged");
                    return;
                }

                itemService.deleteTransaction(UserSnowflake.fromId(transaction.userId()), transaction.transactionId());

                if (transaction.isPlayActivity()) {
                    log.info("Transaction was play activity. Checking if user is still eligible");
                    var rankInfo = bot.getDatabase().getRankService().getUserInfo(UserSnowflake.fromId(transaction.userId()));
                    if (rankInfo.karma() - rankInfo.lastKarma() >= PLAY_ACTIVITY_KARMA_THRESHOLD) {
                        log.info("Adding new play activity to user");
                        itemService.addPlayActivity(UserSnowflake.fromId(transaction.userId()));
                        itemService.updateLastKarma(UserSnowflake.fromId(transaction.userId()));

                        messageUser(transaction, bot.getEmbedCache().getEmbed("playActivityRenew").toEmbedBuilder(), bot);
                        return;
                    }
                    log.info("User didn't reach play activity threshold");
                }

                var item = itemService.getItem(transaction.itemId());
                var emoji = itemService.getTypeEmoji(item.typeId());
                EmbedBuilder embed = bot.getEmbedCache().getEmbed("itemExpired")
                        .injectValue("item", String.format("%s %s", emoji, item.name()))
                        .toEmbedBuilder();

                messageUser(transaction, embed, bot);
                log.info("Item removal finished");
            }, expiring.expiresAt() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            log.info("Scheduling transaction {} for removal in {} ms", expiring, expiring.expiresAt() - System.currentTimeMillis());
        }
    }

    private void messageUser(ItemService.Transaction transaction, EmbedBuilder embed, Bot bot) {
        var user = bot.getJda().getUserById(transaction.userId());
        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed.build()))
                .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, exception -> {
                    TextChannel channel = bot.getDatabase().getSettingsService().getBotChannel();
                    channel.sendMessage(user.getAsMention()).and(channel.sendMessageEmbeds(embed.build())).queue();
                }));
    }

}
