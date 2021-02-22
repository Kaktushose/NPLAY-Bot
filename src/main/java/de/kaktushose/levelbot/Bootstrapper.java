package de.kaktushose.levelbot;

import de.kaktushose.levelbot.bot.Levelbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.security.auth.login.LoginException;

@SpringBootApplication
public class Bootstrapper {

    public static void main(String[] args) throws LoginException, InterruptedException {

        SpringApplication.run(Bootstrapper.class, args);
        Levelbot levelbot = new Levelbot(Levelbot.GuildType.TESTING);
        levelbot.start().indexMembers();
        System.out.println(
        levelbot.getJdaCommands().getCommands().get(0)
        );
    }

}
