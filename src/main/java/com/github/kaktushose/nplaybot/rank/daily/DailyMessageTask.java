package com.github.kaktushose.nplaybot.rank.daily;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.TimeUnit;

public class DailyMessageTask {

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS, startAtMidnight = true)
    public void onSendDailyMessages(Bot bot) {
        bot.getDatabase().getRankService().getDailyRankInfos().forEach((userId, userInfo) ->
                bot.getJda().retrieveUserById(userId)
                        .flatMap(User::openPrivateChannel)
                        .flatMap(channel ->
                                channel.sendMessage(
                                        bot.getEmbedCache().getEmbed(userInfo.nextRank().isPresent() ? "rankInfo" : "rankInfoMax")
                                                .injectValues(userInfo.getEmbedValues(channel.getUser(), true)).toMessageCreateData()
                                )
                        ).queue()
        );
    }

}
