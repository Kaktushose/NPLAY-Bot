package com.github.kaktushose.nplaybot.rank.daily;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class DailyMessageTask {

    private static final Logger log = LoggerFactory.getLogger(DailyMessageTask.class);

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS, startAtMidnight = true)
    public void onSendDailyMessages(Bot bot) {
        bot.getDatabase().getRankService().getDailyRankInfos().forEach(user -> {
                    log.info("Sending daily info message to user {}", user);
                    bot.getGuild().retrieveMember(UserSnowflake.fromId(user.id())).queue(member ->
                            member.getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(
                                    bot.getEmbedCache().getEmbed(user.nextRank().isPresent() ? "rankInfo" : "rankInfoMax")
                                            .injectValues(user.getEmbedValues(member.getUser(), true)).toMessageCreateData())
                            ).queue(), new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER)
                    );
                }
        );
    }
}
