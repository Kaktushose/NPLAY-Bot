package de.kaktushose.levelbot.bot.data;

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
    private String youtubeApiKey;
    private long eventChannelId;
    private String eventEmote;
    private int collectEventId;
    private long statisticsMessageId;
    private long statisticsChannelId;
    private long logChannelId;

    public GuildSettings() {
    }

    public GuildSettings(Long guildId,
                         String version,
                         String botToken,
                         String botPrefix,
                         long botChannelId,
                         long messageCooldown,
                         String youtubeApiKey,
                         long eventChannelId,
                         String eventEmote,
                         int collectEventId,
                         long statisticsMessageId,
                         long statisticsChannelId, long logChannelId) {
        this.guildId = guildId;
        this.version = version;
        this.botToken = botToken;
        this.botPrefix = botPrefix;
        this.botChannelId = botChannelId;
        this.messageCooldown = messageCooldown;
        this.youtubeApiKey = youtubeApiKey;
        this.eventChannelId = eventChannelId;
        this.eventEmote = eventEmote;
        this.collectEventId = collectEventId;
        this.statisticsMessageId = statisticsMessageId;
        this.statisticsChannelId = statisticsChannelId;
        this.logChannelId = logChannelId;
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

    public long getLogChannelId() {
        return logChannelId;
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

    public String getYoutubeApiKey() {
        return youtubeApiKey;
    }

    public void setYoutubeApiKey(String youtubeApiKey) {
        this.youtubeApiKey = youtubeApiKey;
    }

    public long getEventChannelId() {
        return eventChannelId;
    }

    public void setEventChannelId(long eventChannelId) {
        this.eventChannelId = eventChannelId;
    }

    public String getEventEmote() {
        return eventEmote;
    }

    public void setEventEmote(String eventEmote) {
        this.eventEmote = eventEmote;
    }

    public int getCollectEventId() {
        return collectEventId;
    }

    public void setCollectEventId(int collectEventId) {
        this.collectEventId = collectEventId;
    }

    public long getStatisticsMessageId() {
        return statisticsMessageId;
    }

    public void setStatisticsMessageId(long statisticsMessageId) {
        this.statisticsMessageId = statisticsMessageId;
    }

    public long getStatisticsChannelId() {
        return statisticsChannelId;
    }

    public void setStatisticsChannelId(long statisticsChannelId) {
        this.statisticsChannelId = statisticsChannelId;
    }
}
