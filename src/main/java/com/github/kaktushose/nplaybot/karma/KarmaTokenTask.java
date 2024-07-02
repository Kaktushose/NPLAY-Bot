package com.github.kaktushose.nplaybot.karma;

import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;

public class KarmaTokenTask {

    @ScheduledTask(startAtMidnight = true, period = 24, unit = TimeUnit.HOURS)
    public void onResetKarmaTokens(Bot bot) {
        bot.getDatabase().getKarmaService().resetTokens();
    }

}
