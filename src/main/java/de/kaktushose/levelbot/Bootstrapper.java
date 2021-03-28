package de.kaktushose.levelbot;

import de.kaktushose.levelbot.bot.Levelbot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.security.auth.login.LoginException;

@SpringBootApplication
public class Bootstrapper {

    private final static Logger log = LoggerFactory.getLogger(Bootstrapper.class);

    public static void main(String[] args) throws LoginException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("Starting bot...");
        SpringApplication.run(Bootstrapper.class, args);
        Levelbot levelbot = new Levelbot(Levelbot.GuildType.TESTING);
        levelbot.start().indexMembers();
        startTime = System.currentTimeMillis() - startTime;
        log.info("Successfully started bot! Took {} ms", startTime);
    }

}
