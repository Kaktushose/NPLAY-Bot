package com.github.kaktushose.nplaybot.rank.daily;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.concurrent.TimeUnit;

public class DailyMessageTask {

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS, startAtMidnight = true)
    public void onSendDailyMessages(Bot bot) {
        bot.getDatabase().getRankService().getDailyRankInfos().forEach(user ->
                bot.getGuild().retrieveMember(UserSnowflake.fromId(user.id())).queue(member ->
                        member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(
                                bot.getEmbedCache().getEmbed(user.nextRank().isPresent() ? "rankInfo" : "rankInfoMax")
                                        .injectValues(user.getEmbedValues(member.getUser(), true)).toMessageCreateData()
                        )).queue(), new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER))
        );
    }

}
