package de.kaktushose.nrtv.discord.core;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.bot.LevelBot;
import de.kaktushose.nrtv.discord.core.bot.TestBot;
import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class Bootstrapper {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        Logger logger = Logging.getLogger();

        Bot bot = new LevelBot();

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> logger.error("An unexpected error has occurred", e)));

        Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown, "Shutdown-Thread"));

        try {
            bot.start();
        } catch (LoginException | InterruptedException e) {
            logger.error("Can't login to bot account!", e);
            System.exit(1);
        }
        logger.info("Successfully started bot - took " + (System.currentTimeMillis() - startTime) + "ms");

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

        executorService.scheduleAtFixedRate(() -> {
            bot.sendDMs();
            bot.removeItems();
        },0, 24, TimeUnit.HOURS);

        AtomicInteger counter = new AtomicInteger(0);
        bot.getJda().getPresence().setStatus(OnlineStatus.ONLINE);
        executorService.scheduleAtFixedRate(() -> {
            switch (counter.get()) {
                case 0:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.watching("nach Befehlen | !hilfe"));
                    break;
                case 1:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("version: " + bot.getVersion()));
                    break;
                case 2:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("Farming Simulator 19"));
                    break;
                case 3:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("Notruf 112 2"));
                    break;
                case 4:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.hearing("Podcast Simulator 3000"));
                    break;
                case 5:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("Snowrunner"));
                    break;
                case 6:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("The Bus"));
                    break;
                case 7:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("Microsoft Flight Simulator 2020"));
                    break;
                case 8:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("kein Among Us"));
                    break;
                case 9:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.watching("dem Lavendel beim vertrocknen zu"));
                    break;
                case 10:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.playing("TSW 2"));
                    break;
                case 11:
                    counter.set(counter.get() + 1);
                    bot.getJda().getPresence().setActivity(Activity.watching("Ansgar zu"));
                    break;
                case 12:
                    counter.set(0);
                    bot.getJda().getPresence().setActivity(Activity.watching("FarmerTown"));
            }
        }, 0, 15, TimeUnit.SECONDS);

    }

}
