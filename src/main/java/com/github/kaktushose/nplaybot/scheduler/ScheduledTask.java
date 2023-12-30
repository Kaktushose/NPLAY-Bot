package com.github.kaktushose.nplaybot.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledTask {

    boolean repeat() default true;

    /**
     * This will override {@link #initialDelay()}!
     *
     * @return whether this scheduled task should start at midnight
     */
    boolean startAtMidnight() default false;

    long initialDelay() default 0;

    long period();

    TimeUnit unit();

}
