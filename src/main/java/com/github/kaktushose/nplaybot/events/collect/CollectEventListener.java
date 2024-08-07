package com.github.kaktushose.nplaybot.events.collect;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.permissions.PermissionsService;
import com.github.kaktushose.nplaybot.rank.RankService;
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CollectEventListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CollectEventListener.class);
    private final RankService rankService;
    private final CollectEventService eventService;
    private final SettingsService settingsService;
    private final PermissionsService permissionsService;
    private final EmbedCache embedCache;
    private final Set<Long> collectLootDrops;
    private final ItemService itemService;

    public CollectEventListener(Database database, EmbedCache embedCache) {
        this.rankService = database.getRankService();
        this.eventService = database.getCollectEventService();
        this.settingsService = database.getSettingsService();
        this.permissionsService = database.getPermissionsService();
        this.itemService = database.getItemService();
        this.embedCache = embedCache;
        collectLootDrops = new HashSet<>();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        log.debug("Received message event");
        if (!eventService.isCollectEventActive()) {
            return;
        }
        var author = event.getAuthor();

        if (author.isBot()) {
            log.trace("Author is bot");
            return;
        }
        if (!event.isFromGuild()) {
            log.trace("Event is not from guild");
            return;
        }

        if (!rankService.isValidChannel(event.getChannel())) {
            return;
        }

        if (!rankService.isValidMessage(event.getMessage())) {
            return;
        }

        if (!permissionsService.hasUserPermissions(event.getMember())) {
            return;
        }

        onCollectLootDrop(event);
        onAddRegularCollectPoints(event);
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
            itemService.createTransaction(member, reward.itemId());
        }

        var builder = new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(EmbedBuilder.fromData(DataObject.fromJson(reward.embed())).build())
                .build();
        settingsService.getBotChannel().sendMessage(builder).queue();
    }

    private void onCollectLootDrop(MessageReceivedEvent event) {
        var points = eventService.getCollectLootDrop(event.getMessage());

        if (points < 1) {
            log.debug("No collect loot drop for this message");
            return;
        }

        collectLootDrops.add(event.getMessageIdLong());
        event.getMessage().addReaction(Emoji.fromUnicode(eventService.getCollectCurrency().emoji())).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
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

        log.debug("Collect loot drop got claimed by {}", event.getMember());

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
