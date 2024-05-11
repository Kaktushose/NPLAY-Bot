package com.github.kaktushose.nplaybot.scheduler;

import com.github.kaktushose.nplaybot.Bot;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskScheduler.class);
    private static final String BASE_PACKAGE = "com.github.kaktushose.nplaybot";
    private final Reflections reflections;
    private final ScheduledExecutorService executor;
    private final Bot bot;

    public TaskScheduler(Bot bot) {
        this.bot = bot;
        executor = Executors.newScheduledThreadPool(4, runnable -> new Thread(runnable, "TaskScheduler"));
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes, Scanners.MethodsAnnotated)
                .setUrls(ClasspathHelper.forClass(Bot.class))
                .filterInputsBy(new FilterBuilder().includePackage(BASE_PACKAGE));
        reflections = new Reflections(config);

        indexTasks();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void indexTasks() {
        var methods = reflections.getMethodsAnnotatedWith(ScheduledTask.class);
        log.debug("Indexing tasks:");
        for (var method : methods) {
            var scheduledTask = method.getAnnotation(ScheduledTask.class);

            var delay = scheduledTask.unit().toMillis(scheduledTask.initialDelay());
            var period = scheduledTask.unit().toMillis(scheduledTask.period());
            if (scheduledTask.startAtMidnight()) {
                delay = TimeUnit.HOURS.toMinutes(24)
                        - (TimeUnit.HOURS.toMinutes(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                           + Calendar.getInstance().get(Calendar.MINUTE));
                delay = TimeUnit.MINUTES.toMillis(delay);
            }

            if (scheduledTask.repeat()) {
                log.debug("Scheduling repeating task {}.{}. Starts in {} ms. Repeats every {} ms",
                        method.getDeclaringClass().getSimpleName(), method.getName(), delay, period);
                executor.scheduleAtFixedRate(execute(method), delay, period, scheduledTask.unit());
            } else {
                log.debug("Scheduling task {}.{}. Starts in {} ms", method.getDeclaringClass().getSimpleName(), method.getName(), period);
                executor.schedule(execute(method), delay, scheduledTask.unit());
            }
        }
    }

    private Runnable execute(Method method) {
        return () -> {
            try {
                log.info("Invoking task: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
                method.invoke(method.getDeclaringClass().getConstructors()[0].newInstance(), bot);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("Exception in scheduled task!", e);
            }
        };
    }
}
