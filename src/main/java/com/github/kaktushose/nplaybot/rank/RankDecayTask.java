package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class RankDecayTask {

    private static final int START_DECAY_RANK_ID = 2;
    private static final int DECAY_XP_AMOUNT = 150;

    @ScheduledTask(period = 7, unit = TimeUnit.DAYS)
    public void accept(Bot bot) {
        bot.getGuild().loadMembers(member -> {
            var currentRank = bot.getDatabase().getRankService().getUserInfo(member);

            if (currentRank.currentRank().rankId() < START_DECAY_RANK_ID) {
                return;
            }

            var userXpAfterDecay = currentRank.currentXp() - DECAY_XP_AMOUNT;
            var newRank = bot.getDatabase().getRankService().getRankByXp(userXpAfterDecay);

            XpChangeResult result;
            // if new rank is present
            if (newRank.isPresent()) {
                // check if we fall under the minimum rank and set xp to lower bound
                if (newRank.get().rankId() < START_DECAY_RANK_ID) {
                    var minimumRank = bot.getDatabase().getRankService().getRankInfo(START_DECAY_RANK_ID);
                    result = bot.getDatabase().getRankService().setXp(member, minimumRank.orElseThrow().xpBound());
                // if we don't fall under the minimum rank just remove all xp as planned
                } else {
                    result = bot.getDatabase().getRankService().setXp(member, userXpAfterDecay);
                }
            // if new rank isn't present we went too far and also set xp to lower bound
            } else {
                var minimumRank = bot.getDatabase().getRankService().getRankInfo(START_DECAY_RANK_ID);
                result = bot.getDatabase().getRankService().setXp(member, minimumRank.orElseThrow().xpBound());
            }
            bot.getDatabase().getRankService().onXpChange(result, member, bot.getGuild(), bot.getEmbedCache());
        });
    }

}
