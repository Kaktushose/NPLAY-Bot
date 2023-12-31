package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class StatisticsTask {

    @ScheduledTask(period = 24, unit = TimeUnit.HOURS, startAtMidnight = true)
    public void accept(Bot bot) {
        bot.getDatabase().getRankService().resetDailyStatistics();
    }
}
