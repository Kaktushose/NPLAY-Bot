package de.kaktushose.levelbot.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "guild_settings")
public class GuildSettings {

    @Id
    private Long guildId;
    private String version;
    private String botToken;
    private String botPrefix;
    private long botChannelId;
    private long messageCooldown;
    private long shopChannelId;
    private long shopMessageId;

    public GuildSettings() {
    }

    public GuildSettings(Long guildId,
                         String version,
                         String botToken,
                         String botPrefix,
                         long botChannelId,
                         long messageCooldown,
                         long shopChannelId,
                         long shopMessageId) {
        this.guildId = guildId;
        this.version = version;
        this.botToken = botToken;
        this.botPrefix = botPrefix;
        this.botChannelId = botChannelId;
        this.messageCooldown = messageCooldown;
        this.shopChannelId = shopChannelId;
        this.shopMessageId = shopMessageId;
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

    public long getMessageCooldown() {
        return messageCooldown;
    }

    public void setMessageCooldown(long messageCooldown) {
        this.messageCooldown = messageCooldown;
    }

    public long getShopChannelId() {
        return shopChannelId;
    }

    public void setShopChannelId(long shopChannelId) {
        this.shopChannelId = shopChannelId;
    }

    public long getShopMessageId() {
        return shopMessageId;
    }

    public void setShopMessageId(long shopMessageId) {
        this.shopMessageId = shopMessageId;
    }
}
