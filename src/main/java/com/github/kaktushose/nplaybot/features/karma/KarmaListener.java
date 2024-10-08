package com.github.kaktushose.nplaybot.features.karma;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.events.BotEvent;
import com.github.kaktushose.nplaybot.events.reactions.karma.*;
import com.github.kaktushose.nplaybot.features.items.ItemService;
import com.github.kaktushose.nplaybot.features.rank.RankService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.kaktushose.nplaybot.features.items.ItemExpirationTask.PLAY_ACTIVITY_KARMA_THRESHOLD;

public class KarmaListener {

    private static final Logger log = LoggerFactory.getLogger(KarmaListener.class);
    private final Bot bot;
    private final KarmaService karmaService;
    private final RankService rankService;
    private final ItemService itemService;
    private final EmbedCache embedCache;

    public KarmaListener(Bot bot) {
        this.bot = bot;
        karmaService = bot.getDatabase().getKarmaService();
        rankService = bot.getDatabase().getRankService();
        itemService = bot.getDatabase().getItemService();
        this.embedCache = bot.getEmbedCache();
    }

    @BotEvent
    public void onKarmaUpvote(KarmaUpvoteEvent event) {
        int oldKarma = rankService.getUserInfo(event.getMessage().getAuthor()).karma();
        int newKarma = karmaService.onKarmaVoteAdd(event.getJDAEvent().getUser(), event.getMessage().getAuthor(), true);
        event.getEventDispatcher().dispatch(new KarmaBalanceChangeEvent(event.getBot(), oldKarma, newKarma, event.getMessage().getMember()));
    }

    @BotEvent
    public void onKarmaDownvote(KarmaDownvoteEvent event) {
        int oldKarma = rankService.getUserInfo(event.getMessage().getAuthor()).karma();
        int newKarma = karmaService.onKarmaVoteRemove(event.getJDAEvent().getUser(), event.getMessage().getAuthor(), true);

        event.getEventDispatcher().dispatch(new KarmaBalanceChangeEvent(event.getBot(), oldKarma, newKarma, event.getMessage().getMember()));
    }

    @BotEvent
    public void onKarmaChange(KarmaBalanceChangeEvent event) {
        if (event.getNewKarma() > event.getOldKarma()) {
            event.getEventDispatcher().dispatch(new KarmaIncreaseEvent(event));
        }
        if (event.getNewKarma() < event.getOldKarma()) {
            event.getEventDispatcher().dispatch(new KarmaDecreaseEvent(event));
        }
    }

    @BotEvent
    public void onKarmaIncrease(KarmaIncreaseEvent event) {
        var member = event.getMember();
        var rankInfo = rankService.getUserInfo(member);

        // play activity role
        if (event.getNewKarma() - rankInfo.lastKarma() >= PLAY_ACTIVITY_KARMA_THRESHOLD) {
            if (itemService.getTransactions(member).stream().anyMatch(ItemService.Transaction::isPlayActivity)) {
                return;
            }

            log.info("User {} has enough karma for Play Activity", member);
            itemService.addPlayActivity(member);
            itemService.updateLastKarma(member);

            var builder = new MessageCreateBuilder().addContent(member.getAsMention())
                    .addEmbeds(embedCache.getEmbed("playActivityAdd").toMessageEmbed())
                    .build();
            bot.getDatabase().getSettingsService().getBotChannel().sendMessage(builder).queue();
        }

        // karma rewards
        var rewards = karmaService.getKarmaRewards();
        var optional = rewards.stream()
                .filter(it -> it.threshold() > event.getOldKarma())
                .filter(it -> it.threshold() <= event.getNewKarma())
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
            bot.getGuild().addRoleToMember(member, bot.getGuild().getRoleById(reward.roleId())).queue();
        }

        var builder = new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(EmbedBuilder.fromData(DataObject.fromJson(reward.embed())).build())
                .build();
        bot.getDatabase().getSettingsService().getBotChannel().sendMessage(builder).queue();
        log.info("Member {} received a karma reward {}", member, reward);
    }

    @BotEvent
    public void onKarmaDecrease(KarmaDecreaseEvent event) {
        var member = event.getMember();
        var rewards = karmaService.getKarmaRewards();
        var optional = rewards.stream()
                .filter(it -> it.threshold() < event.getOldKarma())
                .filter(it -> it.threshold() >= event.getNewKarma())
                .findFirst();

        if (optional.isEmpty()) {
            return;
        }
        var reward = optional.get();

        if (reward.xp() > 0) {
            var xpChangeResult = rankService.addXp(member, -reward.xp());
            rankService.onXpChange(xpChangeResult, member, embedCache);
        }

        if (reward.roleId() > 0) {
            bot.getGuild().removeRoleFromMember(member, bot.getGuild().getRoleById(reward.roleId())).queue();
        }

        var builder = new MessageCreateBuilder()
                .addContent(member.getAsMention())
                .addEmbeds(embedCache.getEmbed("karmaRewardRemove")
                        .injectValue("user", member.getAsMention())
                        .toEmbedBuilder()
                        .build()
                ).build();
        bot.getDatabase().getSettingsService().getBotChannel().sendMessage(builder).queue();
        log.info("Removed karma reward {} from user {}", reward, member);
    }
}
