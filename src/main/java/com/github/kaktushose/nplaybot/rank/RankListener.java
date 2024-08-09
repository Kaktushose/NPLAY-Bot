package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.events.collect.CollectEventService;
import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import com.github.kaktushose.nplaybot.settings.SettingsService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RankListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(RankListener.class);
    private static final String XP_LOOT_DROP_EMOJI = "\uD83D\uDFE2";
    private final RankService rankService;
    private final PermissionsService permissionsService;
    private final EmbedCache embedCache;
    private final Bot bot;
    private final Map<Long, Integer> xpLootDrops;
    private final Set<Long> collectLootDrops;
    private final CollectEventService eventService;
    private final ItemService itemService;
    private final SettingsService settingsService;

    public RankListener(Database database, EmbedCache embedCache, Bot bot) {
        this.rankService = database.getRankService();
        this.permissionsService = database.getPermissionsService();
        this.eventService = database.getCollectEventService();
        this.embedCache = embedCache;
        this.bot = bot;
        xpLootDrops = new HashMap<>();
        collectLootDrops = new HashSet<>();
        itemService = database.getItemService();
        settingsService = database.getSettingsService();
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

        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }

        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }

        if (!rankService.isValidMessage(event.getMessage())) {
            log.debug("Message doesn't meet rank criteria");
            return;
        }

        onXpLootDrop(event);
        onCheckForLootbox(event);
        onAddRegularXp(event);
        onAddRegularCollectPoints(event);
        onCollectLootDrop(event);
    }

    private void onCheckForLootbox(MessageReceivedEvent event) {
        var lootboxChance = rankService.getLootboxChance();
        var lootboxQueryLimit = rankService.getLootboxQueryLimit();

        if (ThreadLocalRandom.current().nextDouble(100) >= lootboxChance) {
            return;
        }
        event.getChannel().getHistory().retrievePast(lootboxQueryLimit).queue(messages -> {
            var message = messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
            var lootbox = rankService.getRandomLootbox();
            LootboxListener.newListener(bot, lootbox, event.getMember(), message, false);
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
        event.getMessage().addReaction(Emoji.fromUnicode(XP_LOOT_DROP_EMOJI)).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        var messageId = event.getMessageIdLong();
        if (!xpLootDrops.containsKey(messageId)) {
            onCollectEventDropReactionAdd(event);
            return;
        }
        if (!event.getEmoji().equals(Emoji.fromUnicode(XP_LOOT_DROP_EMOJI))) {
            return;
        }

        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        var xp = xpLootDrops.get(messageId);
        var result = rankService.addXp(event.getMember(), xp);
        rankService.onXpChange(result, event.getMember(), embedCache);
        xpLootDrops.remove(messageId);

        log.debug("Xp loot drop got claimed by {}", event.getMember());
        settingsService.getLogChannel().sendMessage(new MessageCreateBuilder()
                .setEmbeds(embedCache.getEmbed("logChannelEntry")
                        .injectValue("title", "XP Loot Drop")
                        .injectValue("description", String.format("%s hat %d XP in einem Loot Drop gefunden",
                                event.getUser().getAsMention(),
                                xp)
                        ).toEmbedBuilder()
                        .setTimestamp(Instant.now())
                        .build()
                ).build()
        ).queue();

        event.retrieveMessage().queue(message -> {
            message.reply(
                    embedCache.getEmbed("xpLootDropClaimed")
                            .injectValue("user", event.getMember().getAsMention())
                            .injectValue("xp", xp)
                            .toMessageCreateData()
            ).mentionRepliedUser(false).queue(it -> it.delete().queueAfter(1, TimeUnit.MINUTES));
            message.clearReactions().queue();
        });
    }

    private void onAddRegularCollectPoints(MessageReceivedEvent event) {
        var oldPoints = eventService.getCollectPoints(event.getAuthor());
        var newPoints = eventService.addCollectPoint(event.getAuthor());
        onCollectPointChange(oldPoints, newPoints, event.getMember(), event.getGuild());
    }

    private void onCollectPointChange(int oldPoints, int newPoints, Member member, Guild guild) {
        var rewards = eventService.getCollectRewards();
        var optional = rewards.stream()
                .filter(it -> it.threshold() > oldPoints)
                .filter(it -> it.threshold() <= newPoints)
                .findFirst();

        if (optional.isEmpty()) {
            return;
        }
        var reward = optional.get();

        if (reward.xp() > 0) {
            var xpChangeResult = rankService.addXp(member, reward.xp());
            rankService.onXpChange(xpChangeResult, member, embedCache);
        }

        if (reward.roleId() > 0) {
            guild.addRoleToMember(member, guild.getRoleById(reward.roleId())).queue();
        }

        if (reward.itemId() > 0) {
            itemService.createTransaction(member, reward.itemId()).ifPresent(role -> {
                log.info("Adding role {} to member {}", member, role);
                guild.addRoleToMember(member, role).queue();
            });
        }

        var builder = new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(EmbedBuilder.fromData(DataObject.fromJson(reward.embed())).build())
                .build();
        settingsService.getBotChannel().sendMessage(builder).queue();

        log.info("Member {} received a collect reward {}", member, reward);
    }

    private void onCollectLootDrop(MessageReceivedEvent event) {
        var points = eventService.getCollectLootDrop(event.getMessage());

        if (points < 1) {
            log.debug("No collect loot drop for this message");
            return;
        }
        log.info("Creating collect loot drop for message {}, points: {}", event.getMessageId(), points);
        collectLootDrops.add(event.getMessageIdLong());
        event.getMessage().addReaction(Emoji.fromUnicode(eventService.getCollectCurrency().emoji())).queue();
    }


    private void onCollectEventDropReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        if (!eventService.isCollectEventActive()) {
            return;
        }
        var messageId = event.getMessageIdLong();
        if (!collectLootDrops.contains(messageId)) {
            return;
        }
        var currency = eventService.getCollectCurrency();
        if (!event.getEmoji().equals(Emoji.fromUnicode(currency.emoji()))) {
            return;
        }

        if (!permissionsService.hasUserPermissions(event.getMember())) {
            event.getReaction().removeReaction(event.getUser()).queue();
            return;
        }

        log.info("Collect loot drop {} got claimed by {}", event.getMessageId(), event.getMember());
        settingsService.getLogChannel().sendMessage(new MessageCreateBuilder()
                .setEmbeds(embedCache.getEmbed("logChannelEntry")
                        .injectValue("title", "Collect Loot Drop")
                        .injectValue("description", String.format("%s hat einen Collect Punkt in einem Loot Drop gefunden",
                                event.getUser().getAsMention())
                        ).toEmbedBuilder()
                        .setTimestamp(Instant.now())
                        .build()
                ).build()
        ).queue();

        var oldPoints = eventService.getCollectPoints(event.getMember());
        var newPoints = eventService.addCollectPoint(event.getMember());
        onCollectPointChange(oldPoints, newPoints, event.getMember(), event.getGuild());
        collectLootDrops.remove(messageId);

        event.retrieveMessage().queue(message -> {
            message.reply(
                    embedCache.getEmbed("collectLootDropClaimed")
                            .injectValue("user", event.getMember().getAsMention())
                            .injectValue("name", currency.name())
                            .toMessageCreateData()
            ).mentionRepliedUser(false).queue(it -> it.delete().queueAfter(1, TimeUnit.MINUTES));
            message.clearReactions().queue();
        });
    }

}
