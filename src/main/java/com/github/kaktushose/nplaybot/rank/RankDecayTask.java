package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RankDecayTask {

    private static final Logger log = LoggerFactory.getLogger(RankDecayTask.class);

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS)
    public void accept(Bot bot) {
        var rankService = bot.getDatabase().getRankService();
        var startDecayRankId = rankService.getStartDecayRankId();
        var rankDecayInterval = rankService.getRankDecayInterval();
        var decayXp = rankService.getDecayXp();

        if ((System.currentTimeMillis() - rankService.getLastDecay()) < TimeUnit.DAYS.toMillis(rankDecayInterval)) {
            log.info("Last decay is less then {} days ago", rankDecayInterval);
            return;
        }

        rankService.updateLastDecay();

        log.info("Checking for rank decay");
        rankService.getUsersForRankDecay().forEach(user -> {
            if ((System.currentTimeMillis() - user.lastValidMessage()) < TimeUnit.DAYS.toMillis(rankDecayInterval)) {
                return;
            }

            var userXpAfterDecay = user.currentXp() - decayXp;
            var newRank = bot.getDatabase().getRankService().getRankByXp(userXpAfterDecay);

            var userSnowflake = UserSnowflake.fromId(user.id());
            XpChangeResult result;
            // if new rank is present
            if (newRank.isPresent()) {
                // check if we fall under the minimum rank and set xp to lower bound
                if (newRank.get().rankId() < startDecayRankId) {
                    var minimumRank = bot.getDatabase().getRankService().getRankInfo(startDecayRankId);
                    result = bot.getDatabase().getRankService().setXp(userSnowflake, minimumRank.orElseThrow().xpBound());
                    // if we don't fall under the minimum rank just remove all xp as planned
                } else {
                    result = bot.getDatabase().getRankService().setXp(userSnowflake, userXpAfterDecay);
                }
                // if new rank isn't present we went too far and also set xp to lower bound
            } else {
                var minimumRank = bot.getDatabase().getRankService().getRankInfo(startDecayRankId);
                result = bot.getDatabase().getRankService().setXp(userSnowflake, minimumRank.orElseThrow().xpBound());
            }

            bot.getGuild().retrieveMemberById(user.id()).queue(member -> bot.getDatabase().getRankService().onXpChange(result, member, bot.getEmbedCache()));
        });
    }
}
