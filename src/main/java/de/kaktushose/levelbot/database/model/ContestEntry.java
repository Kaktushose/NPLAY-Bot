package de.kaktushose.levelbot.database.model;

import de.kaktushose.levelbot.util.Pageable;
import de.kaktushose.levelbot.util.Pagination;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "contest_entries")
public class ContestEntry implements Pageable {

    @Id
    private long messageId;
    private long count;
    private long userId;

    public ContestEntry() {
    }

    public ContestEntry(long messageId, long userId, long count) {
        this.messageId = messageId;
        this.count = count;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCount(Pagination.CurrencyType currencyType) {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getCount() {
        return count;
    }
}
