package de.kaktushose.levelbot.account.data;

import de.kaktushose.levelbot.shop.data.transactions.Transaction;
import de.kaktushose.levelbot.util.Pageable;
import de.kaktushose.levelbot.util.Pagination;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class BotUser implements Pageable {

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
    private int rewardLevel;
    private long lastReward;
    private long eventPoints;
    @OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private List<Transaction> transactions;

    public BotUser() {
        level = 1;
    }

    public BotUser(long userId) {
        this.userId = userId;
        level = 1;
        permissionLevel = 1;
        coins = 100;
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
                   int rewardLevel,
                   long lastReward,
                   long eventPoints,
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
        this.rewardLevel = rewardLevel;
        this.lastReward = lastReward;
        this.eventPoints = eventPoints;
        this.transactions = transactions;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public long getCount(Pagination.CurrencyType currencyType) {
        return switch (currencyType) {
            case XP -> getXp();
            case DIAMONDS -> getDiamonds();
            case COINS -> getCoins();
            default -> 0;
        };
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

    public int getRewardLevel() {
        return rewardLevel;
    }

    public void setRewardLevel(int rewardLevel) {
        this.rewardLevel = rewardLevel;
    }

    public long getLastReward() {
        return lastReward;
    }

    public void setLastReward(long lastReward) {
        this.lastReward = lastReward;
    }

    public long getEventPoints() {
        return eventPoints;
    }

    public void setEventPoints(long eventPoints) {
        this.eventPoints = eventPoints;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
