package de.kaktushose.levelbot.database.model;

import javax.persistence.*;

@Entity
@Table(name = "rewards")
public class Rewards {

    @Id
    private Integer rewardId;
    private int coins;
    private int xp;
    private int diamonds;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId")
    private Item item;
    private String message;

    public Rewards() {
    }

    public Rewards(Integer rewardId, int coins, int xp, int diamonds, Item item, String message) {
        this.rewardId = rewardId;
        this.coins = coins;
        this.xp = xp;
        this.diamonds = diamonds;
        this.item = item;
        this.message = message;
    }

    public Integer getRewardId() {
        return rewardId;
    }

    public void setRewardId(Integer rewardId) {
        this.rewardId = rewardId;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
