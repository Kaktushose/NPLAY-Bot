package de.kaktushose.nrtv.discord.frameworks.command;

public enum PermissionLevel {

    MUTED(0),
    MEMBER(1),
    MODERATOR(2),
    ADMIN(3),
    BOTOWNER(4);

    public final int level;

    PermissionLevel(int level) {
        this.level = level;
    }

}
