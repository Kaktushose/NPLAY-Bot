package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LootboxListener extends ListenerAdapter {

    private static final String LOOTBOX_EMOJI = "\uD83C\uDF81";
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Bot bot;
    private final RankService.Lootbox lootbox;
    private final UserSnowflake user;
    private final long messageId;

    private LootboxListener(Bot bot, RankService.Lootbox lootbox, UserSnowflake user, Message message) {
        this.bot = bot;
        this.lootbox = lootbox;
        this.user = user;
        this.messageId = message.getIdLong();
        bot.getJda().addEventListener(this);
        message.addReaction(Emoji.fromFormatted(LOOTBOX_EMOJI)).queue();
        executor.schedule(() -> terminate(message), 30, TimeUnit.MINUTES);
    }

    public static void newListener(Bot bot, RankService.Lootbox lootbox, UserSnowflake user, Message message) {
        new LootboxListener(bot, lootbox, user, message);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != messageId) {
            return;
        }
        if (event.getUserIdLong() != user.getIdLong()) {
            return;
        }
        if (!event.getReaction().getEmoji().equals(Emoji.fromFormatted(LOOTBOX_EMOJI))) {
            return;
        }

        event.getReaction().clearReactions().queue();

        if (lootbox.xpReward() > 0) {
            var result = bot.getDatabase().getRankService().addXp(user, lootbox.xpReward());
            bot.getDatabase().getRankService().onXpChange(result, event.getMember(), bot.getEmbedCache());
            event.getChannel().sendMessage(user.getAsMention())
                    .and(event.getChannel().sendMessage(bot.getEmbedCache().getEmbed("onLootboxXp").injectValue("xp", lootbox.xpReward()).toMessageCreateData()))
                    .queue();
            return;
        }

        if (lootbox.karmaReward() > 0) {
            var oldKarma = bot.getDatabase().getRankService().getUserInfo(user).karma();
            var newKarma = oldKarma + lootbox.karmaReward();
            bot.getDatabase().getKarmaService().addKarma(user, lootbox.karmaReward());
            bot.getDatabase().getKarmaService().onKarmaIncrease(oldKarma, newKarma, event.getMember(), bot.getEmbedCache());
            event.getChannel().sendMessage(user.getAsMention())
                    .and(event.getChannel().sendMessage(bot.getEmbedCache().getEmbed("onLootboxKarma").injectValue("karma", lootbox.karmaReward()).toMessageCreateData()))
                    .queue();
            return;
        }

        if (lootbox.itemId() >= 0) {
            bot.getDatabase().getItemService().createTransaction(user, lootbox.itemId());
            var item = bot.getDatabase().getItemService().getItem(lootbox.itemId());
            var emoji = bot.getDatabase().getItemService().getTypeEmoji(item.typeId());
            event.getChannel().sendMessage(user.getAsMention())
                    .and(event.getChannel().sendMessage(bot.getEmbedCache().getEmbed("onLootboxItem")
                            .injectValue("item", String.format("%s %s", emoji, item.name())).toMessageCreateData()))
                    .queue();
        }
    }

    private void terminate(Message message) {
        message.clearReactions().queue();
        bot.getJda().removeEventListener(this);
    }

}
