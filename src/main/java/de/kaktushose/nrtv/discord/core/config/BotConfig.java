package de.kaktushose.nrtv.discord.core.config;

public class BotConfig {

    private String prefix, token, version;
    private long guildId, botChannelId;
    private int presentEventItem;
    private boolean eventIsPresent;

    public BotConfig(String prefix, String token, String version, long guildId, long botChannelId, int presentEventItem, boolean eventIsPresent) {
        this.prefix = prefix;
        this.token = token;
        this.version = version;
        this.guildId = guildId;
        this.botChannelId = botChannelId;
        this.presentEventItem = presentEventItem;
        this.eventIsPresent = eventIsPresent;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public long getBotChannelId() {
        return botChannelId;
    }

    public void setBotChannelId(long botChannelId) {
        this.botChannelId = botChannelId;
    }

    public boolean eventItemIsPresent() {
        return presentEventItem > -1;
    }

    public boolean eventIsPresent() {
        return eventIsPresent;
    }

    public void setEventIsPresent(boolean eventIsPresent) {
        this.eventIsPresent = eventIsPresent;
    }

    public void setPresentEventItem(int presentEventItem) {
        this.presentEventItem = presentEventItem;
    }

    public int getPresentEventItem() {
        return presentEventItem;
    }
}
