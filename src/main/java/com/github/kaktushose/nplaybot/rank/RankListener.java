package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RankListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RankListener.class);
    private final RankService rankService;
    private final EmbedCache embedCache;
    private final Bot bot;
    private final Map<Long, Integer> xpLootDrops;

    public RankListener(Database database, EmbedCache embedCache, Bot bot) {
        this.rankService = database.getRankService();
        this.embedCache = embedCache;
        this.bot = bot;
        xpLootDrops = new HashMap<>();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        log.debug("Received message event");
        var author = event.getAuthor();

        if (author.isBot()) {
            log.trace("Author is bot");
            return;
        }
        if (!event.isFromGuild()) {
            log.trace("Event is not from guild");
            return;
        }

        rankService.updateRankRoles(event.getMember(), rankService.getUserInfo(author).currentRank());
        rankService.increaseTotalMessageCount();

        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }

        onXpLootDrop(event);

        onCheckForLootbox(event);

        if (!rankService.isValidMessage(event.getMessage())) {
            log.debug("Message doesn't meet rank criteria");
            return;
        }

        onAddRegularXp(event);
    }

    private void onCheckForLootbox(MessageReceivedEvent event) {
        var lootboxChance = rankService.getLootboxChance();
        var lootboxQueryLimit = rankService.getLootboxQueryLimit();

        if (ThreadLocalRandom.current().nextDouble(100) >= lootboxChance) {
            return;
        }
        event.getChannel().getHistory().retrievePast(lootboxQueryLimit).queue(messages -> {
            var message = messages.get(ThreadLocalRandom.current().nextInt(lootboxQueryLimit));
            var lootbox = rankService.getRandomLootbox();
            LootboxListener.newListener(bot, lootbox, event.getMember(), message);
        });
    }

    private void onAddRegularXp(MessageReceivedEvent event) {
        rankService.updateValidMessage(event.getAuthor());
        var result = rankService.addRandomXp(event.getAuthor());

        rankService.onXpChange(result, event.getMember(), embedCache);
    }

    private void onXpLootDrop(MessageReceivedEvent event) {
        var xp = rankService.getXpLootDrop(event.getMessage());

        if (xp < 1) {
            log.debug("No xp loot drop for this message");
            return;
        }

        xpLootDrops.put(event.getMessageIdLong(), xp);
        event.getMessage().addReaction(Emoji.fromUnicode("\uD83C\uDF1F")).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        var messageId = event.getMessageIdLong();
        if (!xpLootDrops.containsKey(messageId)) {
            return;
        }
        if (!event.getEmoji().equals(Emoji.fromUnicode("\uD83C\uDF1F"))) {
            return;
        }

        log.debug("Xp loot drop got claimed by {}", event.getMember());

        var xp = xpLootDrops.get(messageId);
        var result = rankService.addXp(event.getMember(), xp);
        rankService.onXpChange(result, event.getMember(), embedCache);
        xpLootDrops.remove(messageId);

        event.retrieveMessage().queue(message -> {
            message.reply(
                    embedCache.getEmbed("xpLootDropClaimed")
                            .injectValue("user", event.getMember().getAsMention())
                            .injectValue("xp", xp)
                            .toMessageCreateData()
            ).mentionRepliedUser(false).queue(it -> it.delete().queueAfter(10, TimeUnit.SECONDS));
            message.clearReactions().queue();
        });
    }
}
