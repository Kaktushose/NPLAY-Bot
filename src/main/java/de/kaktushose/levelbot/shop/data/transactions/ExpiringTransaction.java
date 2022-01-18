package de.kaktushose.levelbot.shop.data.transactions;

import de.kaktushose.levelbot.shop.data.items.Item;

public class ExpiringTransaction extends Transaction {

    private long duration;

    public ExpiringTransaction() {
    }

    public ExpiringTransaction(Long transactionId, Item item, long buyTime, long duration) {
        super(transactionId, item, buyTime);
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
