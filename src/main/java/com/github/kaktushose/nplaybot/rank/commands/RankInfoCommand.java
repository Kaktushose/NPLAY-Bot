package com.github.kaktushose.nplaybot.rank.commands;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.concurrent.TimeUnit;

@Interaction
@Permissions(BotPermissions.USER)
public class RankInfoCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "info", isGuildOnly = true, desc = "Zeigt die Kontoinformationen zu einem User an")
    public void onRankInfo(CommandEvent event, @Optional Member member) {
        var target = member == null ? event.getMember() : member;
        UserInfo userInfo = database.getRankService().getUserInfo(target);
        database.getRankService().updateRankRoles(target, userInfo.currentRank());
        database.getItemService().updateItemRoles(target);
        sendReply(userInfo, target.getUser(), event);
    }

    @ContextCommand(value = "Kontoinformation abrufen", type = Command.Type.USER, isGuildOnly = true, ephemeral = true)
    public void onContextRankInfo(CommandEvent event, User user) {
        UserInfo userInfo = database.getRankService().getUserInfo(user);
        sendReply(userInfo, user, event);
    }

    private void sendReply(UserInfo userInfo, User user, CommandEvent event) {
        var embed = embedCache.getEmbed(userInfo.nextRank().isPresent() ? "rankInfo" : "rankInfoMax")
                .injectValues(userInfo.getEmbedValues(user, false))
                .toEmbedBuilder();

        if (database.getCollectEventService().isCollectEventActive()) {
            var currency = database.getCollectEventService().getCollectCurrency();
            var points = database.getCollectEventService().getCollectPoints(user);
            embed.addField(String.format("%s:", currency.name()), String.format("%s %d", currency.emoji(), points), false);

            StringBuilder rewardText = new StringBuilder();
            database.getCollectEventService().getCollectRewards().forEach(reward -> {
                if (userInfo.collectPoints() >= reward.threshold()) {
                    rewardText.append(reward.name()).append("\n");
                }
            });
            if (rewardText.isEmpty()) {
                rewardText.append("❌ noch keine Belohnung");
            }
            var name = database.getCollectEventService().getEventName();
            embed.addField(String.format("%s Belohnungen: ", name), rewardText.toString(), false);
        }

        var transactions = database.getItemService().getTransactions(user);
        if (!transactions.isEmpty()) {
            var items = new StringBuilder();
            transactions.forEach(it -> {
                items.append(database.getItemService().getTypeEmoji(it.typeId()))
                        .append(" ")
                        .append(it.name());
                if (it.expiresAt() > 0) {
                    items.append(" (läuft ab ")
                            .append(String.format("<t:%d:R>", TimeUnit.MILLISECONDS.toSeconds(it.expiresAt())))
                            .append(")\n");
                }
            });
            embed.addField("Items:", items.toString(), false);
        }
        event.reply(embed);
    }
}
