package de.kaktushose.levelbot.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "muted_channels")
public class MutedChannel {

    @Id
    private Long channelId;
    private String reason;
    private long timestamp;
    private long executorId;

    public MutedChannel() {
    }

    public MutedChannel(Long channelId, String reason, long timestamp, long executorId) {
        this.channelId = channelId;
        this.reason = reason;
        this.timestamp = timestamp;
        this.executorId = executorId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExecutorId() {
        return executorId;
    }

    public void setExecutorId(long executorId) {
        this.executorId = executorId;
    }
}
