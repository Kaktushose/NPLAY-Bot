package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.nplaybot.scheduler.ScheduledTask;

import java.util.function.Consumer;

@ScheduledTask
public class UserStatisticsTask implements Consumer<RankService> {

    @Override
    public void accept(RankService rankService) {

    }
}
