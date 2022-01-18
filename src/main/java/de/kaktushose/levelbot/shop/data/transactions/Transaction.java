package de.kaktushose.levelbot.shop.data.transactions;

import de.kaktushose.levelbot.shop.data.items.Item;

import javax.persistence.*;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long transactionId;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId")
    private Item item;
    private long buyTime;

    public Transaction() {
    }

    public Transaction(Long transactionId, Item item, long buyTime) {
        this.transactionId = transactionId;
        this.item = item;
        this.buyTime = buyTime;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public long getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(long buyTime) {
        this.buyTime = buyTime;
    }
}
