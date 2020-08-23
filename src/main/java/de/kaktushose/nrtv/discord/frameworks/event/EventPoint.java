package de.kaktushose.nrtv.discord.frameworks.event;

public class EventPoint {

    private String name;
    private String emote;

    public EventPoint(String name, String emote) {
        this.name = name;
        this.emote = emote;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmote() {
        return emote;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }
}
