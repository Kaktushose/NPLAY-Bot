package de.kaktushose.levelbot.bot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {

    private final ScheduledExecutorService executorService;

    public TaskScheduler() {
        executorService = Executors.newScheduledThreadPool(10);
    }

    public void addSingleTask(Runnable runnable, long delay, TimeUnit timeUnit) {
        executorService.schedule(runnable, delay, timeUnit);
    }

    public void addRepetitiveTask(Runnable runnable, long initialDelay, long period, TimeUnit timeUnit) {
        executorService.scheduleAtFixedRate(runnable, initialDelay, period, timeUnit);
    }

}
