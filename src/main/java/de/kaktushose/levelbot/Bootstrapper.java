package de.kaktushose.levelbot;

import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.services.*;
import de.kaktushose.levelbot.shop.data.ShopService;
import net.dv8tion.jda.api.JDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.security.auth.login.LoginException;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Bootstrapper {

    private final static Logger log = LoggerFactory.getLogger(Bootstrapper.class);
    private static Levelbot levelbot;

    public static void main(String[] args) throws LoginException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("Starting bot...");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> log.error("An unexpected error has occurred!", e));
        SpringApplication.run(Bootstrapper.class, args);
        levelbot = new Levelbot(Levelbot.GuildType.TESTING);
        levelbot.start().indexMembers();
        startTime = System.currentTimeMillis() - startTime;
        log.info("Successfully started bot! Took {} seconds", TimeUnit.MILLISECONDS.toSeconds(startTime));
    }

    @Produces
    public UserService getUserService() {
        return levelbot.getUserService();
    }

    @Produces
    public ShopService getShopService() {
        return levelbot.getShopService();
    }

    @Produces
    public LevelService getLevelService() {
        return levelbot.getLevelService();
    }

    @Produces
    public SettingsService getSettingsService() {
        return levelbot.getSettingsService();
    }

    @Produces
    public BoosterService getBoosterService() {
        return levelbot.getBoosterService();
    }

    @Produces
    public EventService getEventService() {
        return levelbot.getEventService();
    }

    @Produces
    public JDA getJda() {
        return levelbot.getJda();
    }

    @Produces
    public EmbedCache getEmbedCache() {
        return levelbot.getEmbedCache();
    }

    @Produces
    public Levelbot getLevelbot() {
        return levelbot;
    }
}
