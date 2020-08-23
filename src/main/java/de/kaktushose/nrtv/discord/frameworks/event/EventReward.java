package de.kaktushose.nrtv.discord.frameworks.event;

import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;

public abstract class EventReward {

    private int bound;
    private String name;

    public EventReward(int bound, String name) {
        this.bound = bound;
        this.name = name;
    }

    public abstract void onReward(BotUser botUser, Bot bot);

    public int getBound() {
        return bound;
    }

    public String getName() {
        return name;
    }
}
