package com.github.kaktushose.nplaybot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Bootstrapper {

    private final static Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("Starting NPLAY-Bot...");

            var bot = Bot.start(Long.parseLong(System.getenv("BOT_GUILD")));
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("An uncaught exception has occurred!", e));
            Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));

            log.info("Successfully started NPLAY-Bot! Took {} ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Failed to start!", e);
            System.exit(1);
        }
    }
}
