package de.kaktushose.nrtv.discord.core.config;

public enum BotConfigType {

    LEVELBOT(0),
    TESTBOT(1),
    CANARY(2);

    BotConfigType(int id) {
        this.id = id;
    }

    public final int id;

}
