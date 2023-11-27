package com.github.kaktushose.nplaybot;

import com.github.kaktushose.jda.commands.JDACommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {

    private final JDA jda;
    private final JDACommands jdaCommands;

    private Bot(String token) throws InterruptedException {
        jda = JDABuilder.createDefault(token)
                .setActivity(Activity.customStatus("starting..."))
                .setStatus(OnlineStatus.IDLE)
                .build().awaitReady();

        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.customStatus("Version 3.0.2"));

        jdaCommands = JDACommands.start(jda, Bot.class, "com.github.kaktushose.nplaybot");
    }

    public static Bot start(String token) throws InterruptedException {
        return new Bot(token);
    }

    public void shutdown() {
        jdaCommands.shutdown();
        jda.shutdown();
    }
}
