package de.kaktushose.levelbot.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "settings")
public class Config {

    @Id
    private Long guildId;
    private String version;
    private String botToken;
    private String botPrefix;
    private long botChannelId;

    public Config() {
    }

    public Config(long guildId, String version, String botToken, String botPrefix, long botChannelId) {
        this.guildId = guildId;
        this.version = version;
        this.botToken = botToken;
        this.botPrefix = botPrefix;
        this.botChannelId = botChannelId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotPrefix() {
        return botPrefix;
    }

    public void setBotPrefix(String botPrefix) {
        this.botPrefix = botPrefix;
    }

    public long getBotChannelId() {
        return botChannelId;
    }

    public void setBotChannelId(long botChannelId) {
        this.botChannelId = botChannelId;
    }
}
