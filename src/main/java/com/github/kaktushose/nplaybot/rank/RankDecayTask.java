package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class RankDecayTask {

    @ScheduledTask(period = 7, unit = TimeUnit.DAYS)
    public void accept(Bot bot) {
        var startDecayRankId = bot.getDatabase().getRankService().getStartDecayRankId();
        var decayXp = bot.getDatabase().getRankService().getDecayXp();

        bot.getGuild().loadMembers(member -> {
            var currentRank = bot.getDatabase().getRankService().getUserInfo(member);

            if (currentRank.currentRank().rankId() < startDecayRankId) {
                return;
            }

            var userXpAfterDecay = currentRank.currentXp() - decayXp;
            var newRank = bot.getDatabase().getRankService().getRankByXp(userXpAfterDecay);

            XpChangeResult result;
            // if new rank is present
            if (newRank.isPresent()) {
                // check if we fall under the minimum rank and set xp to lower bound
                if (newRank.get().rankId() < startDecayRankId) {
                    var minimumRank = bot.getDatabase().getRankService().getRankInfo(startDecayRankId);
                    result = bot.getDatabase().getRankService().setXp(member, minimumRank.orElseThrow().xpBound());
                    // if we don't fall under the minimum rank just remove all xp as planned
                } else {
                    result = bot.getDatabase().getRankService().setXp(member, userXpAfterDecay);
                }
                // if new rank isn't present we went too far and also set xp to lower bound
            } else {
                var minimumRank = bot.getDatabase().getRankService().getRankInfo(startDecayRankId);
                result = bot.getDatabase().getRankService().setXp(member, minimumRank.orElseThrow().xpBound());
            }
            bot.getDatabase().getRankService().onXpChange(result, member, bot.getEmbedCache());
        });
    }
}
