package de.kaktushose.levelbot.database.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class BotUser {

    @Id
    private Long userId;
    private int level;
    private long xp;
    private long coins;
    private long diamonds;
    private long lastValidMessage;
    private long messageCount;
    private long startXp;
    private long startCoins;
    private long startDiamonds;
    @Column(name = "daily")
    private boolean dailyUpdate;
    private int permissionLevel;
    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private List<Transaction> transactions;

    public BotUser() {
    }

    public BotUser(long userId) {
        this.userId = userId;
    }

    public BotUser(long userId,
                   int level,
                   long xp,
                   long coins,
                   long diamonds,
                   long lastValidMessage,
                   long messageCount,
                   long startXp,
                   long startCoins,
                   long startDiamonds,
                   boolean dailyUpdate,
                   int permissionLevel,
                   List<Transaction> transactions) {
        this.userId = userId;
        this.level = level;
        this.xp = xp;
        this.coins = coins;
        this.diamonds = diamonds;
        this.lastValidMessage = lastValidMessage;
        this.messageCount = messageCount;
        this.startXp = startXp;
        this.startCoins = startCoins;
        this.startDiamonds = startDiamonds;
        this.dailyUpdate = dailyUpdate;
        this.permissionLevel = permissionLevel;
        this.transactions = transactions;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public long getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(long diamonds) {
        this.diamonds = diamonds;
    }

    public long getLastValidMessage() {
        return lastValidMessage;
    }

    public void setLastValidMessage(long lastValidMessage) {
        this.lastValidMessage = lastValidMessage;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public long getStartXp() {
        return startXp;
    }

    public void setStartXp(long startXp) {
        this.startXp = startXp;
    }

    public long getStartCoins() {
        return startCoins;
    }

    public void setStartCoins(long startCoins) {
        this.startCoins = startCoins;
    }

    public long getStartDiamonds() {
        return startDiamonds;
    }

    public void setStartDiamonds(long startDiamonds) {
        this.startDiamonds = startDiamonds;
    }

    public boolean isDailyUpdate() {
        return dailyUpdate;
    }

    public void setDailyUpdate(boolean dailyUpdate) {
        this.dailyUpdate = dailyUpdate;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
