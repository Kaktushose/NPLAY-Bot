package com.github.kaktushose.nplaybot.rank.daily;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.TimeUnit;

public class DailyMessageTask {

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS, startAtMidnight = true)
    public void onSendDailyMessages(Bot bot) {
        bot.getDatabase().getRankService().getDailyRankInfos().forEach(user ->
                bot.getJda().retrieveUserById(user.id())
                        .flatMap(User::openPrivateChannel)
                        .flatMap(channel ->
                                channel.sendMessage(
                                        bot.getEmbedCache().getEmbed(user.nextRank().isPresent() ? "rankInfo" : "rankInfoMax")
                                                .injectValues(user.getEmbedValues(channel.getUser(), true)).toMessageCreateData()
                                )
                        ).queue()
        );
    }

}
