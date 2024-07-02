package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
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
    private final boolean isPrivate;

    private LootboxListener(Bot bot, RankService.Lootbox lootbox, UserSnowflake user, Message message, boolean isPrivate) {
        this.bot = bot;
        this.lootbox = lootbox;
        this.user = user;
        this.messageId = message.getIdLong();
        this.isPrivate = isPrivate;
        bot.getJda().addEventListener(this);
        message.addReaction(Emoji.fromFormatted(LOOTBOX_EMOJI)).queue();
        executor.schedule(() -> terminate(message), 30, TimeUnit.MINUTES);
    }

    public static void newListener(Bot bot, RankService.Lootbox lootbox, UserSnowflake user, Message message, boolean isPrivate) {
        new LootboxListener(bot, lootbox, user, message, isPrivate);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != messageId) {
            return;
        }
        if (event.getUser().isBot()) {
            return;
        }
        UserSnowflake target;
        if (isPrivate) {
            if (event.getUserIdLong() != user.getIdLong()) {
                event.getReaction().removeReaction(event.getUser()).queue();
                return;
            }
            target = user;
        } else {
            target = event.getUser();
        }

        if (!event.getReaction().getEmoji().equals(Emoji.fromFormatted(LOOTBOX_EMOJI))) {
            return;
        }

        event.getReaction().clearReactions().queue();

        if (lootbox.xpReward() > 0) {
            var result = bot.getDatabase().getRankService().addXp(target, lootbox.xpReward());
            bot.getDatabase().getRankService().onXpChange(result, event.getMember(), bot.getEmbedCache());
            var message = new MessageCreateBuilder()
                    .addContent(target.getAsMention())
                            .addEmbeds(bot.getEmbedCache().getEmbed("onLootboxXp").injectValue("xp", lootbox.xpReward()).toMessageEmbed());

            event.getChannel().sendMessage(message.build()).queue(it -> it.delete().queueAfter(1, TimeUnit.MINUTES));
            return;
        }

        if (lootbox.karmaReward() > 0) {
            var oldKarma = bot.getDatabase().getRankService().getUserInfo(target).karma();
            var newKarma = oldKarma + lootbox.karmaReward();
            bot.getDatabase().getKarmaService().addKarma(target, lootbox.karmaReward());
            bot.getDatabase().getKarmaService().onKarmaIncrease(oldKarma, newKarma, event.getMember(), bot.getEmbedCache());
            var message = new MessageCreateBuilder()
                    .addContent(target.getAsMention())
                    .addEmbeds(bot.getEmbedCache().getEmbed("onLootboxKarma").injectValue("karma", lootbox.karmaReward()).toMessageEmbed());

            event.getChannel().sendMessage(message.build()).queue(it -> it.delete().queueAfter(1, TimeUnit.MINUTES));
            return;
        }

        if (lootbox.itemId() >= 0) {
            bot.getDatabase().getItemService().createTransaction(target, lootbox.itemId());
            var item = bot.getDatabase().getItemService().getItem(lootbox.itemId());
            var emoji = bot.getDatabase().getItemService().getTypeEmoji(item.typeId());
            var message = new MessageCreateBuilder()
                    .addContent(target.getAsMention())
                    .addEmbeds(bot.getEmbedCache().getEmbed("onLootboxItem")
                            .injectValue("item", String.format("%s %s", emoji, item.name())).toMessageEmbed());

            event.getChannel().sendMessage(message.build()).queue(it -> it.delete().queueAfter(1, TimeUnit.MINUTES));

        }
    }

    private void terminate(Message message) {
        message.clearReactions().queue();
        bot.getJda().removeEventListener(this);
    }

}
