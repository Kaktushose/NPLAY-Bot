package de.kaktushose.nrtv.discord.core.database.data;

import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;

import java.util.Collections;
import java.util.Map;

public class BotUser {

    private long id, lastXp, premiumBuyTime, boosterBuyTime, djBuyTime, nickNameBuyTime, xpBoosterBuyTime;
    private int level, xp, coins, permissionLevel, startXp, startCoins, messages, eventPoints, diamonds, startDiamonds;
    private boolean daily;
    private Map<ItemType, Item> itemStack;

    public BotUser(long id,
                   long lastXp,
                   long premiumBuyTime,
                   long boosterBuyTime,
                   long djBuyTime,
                   long nickNameBuyTime,
                   long xpBoosterBuyTime,
                   int level,
                   int xp,
                   int coins,
                   int diamonds,
                   int permissionLevel,
                   int startCoins,
                   int startXp,
                   int messages,
                   int eventPoints,
                   int startDiamonds,
                   boolean daily,
                   Map<ItemType, Item> itemStack) {
        this.id = id;
        this.lastXp = lastXp;
        this.premiumBuyTime = premiumBuyTime;
        this.boosterBuyTime = boosterBuyTime;
        this.djBuyTime = djBuyTime;
        this.nickNameBuyTime = nickNameBuyTime;
        this.xpBoosterBuyTime = xpBoosterBuyTime;
        this.level = level;
        this.xp = xp;
        this.coins = coins;
        this.diamonds = diamonds;
        this.permissionLevel = permissionLevel;
        this.startCoins = startCoins;
        this.startXp = startXp;
        this.messages = messages;
        this.eventPoints = eventPoints;
        this.itemStack = itemStack;
        this.startDiamonds = startDiamonds;
        this.daily = daily;
    }

    public BotUser(long id) {
        this.id = id;
        this.lastXp = 0;
        this.premiumBuyTime = 0;
        this.boosterBuyTime = 0;
        this.djBuyTime = 0;
        this.nickNameBuyTime = 0;
        this.level = 0;
        this.xp = 0;
        this.coins = 0;
        this.itemStack = Collections.emptyMap();
        eventPoints = 0;
        this.permissionLevel = 1;
        this.xpBoosterBuyTime = 0;
        this.diamonds = 0;
        startDiamonds = 0;
        daily = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLastXp() {
        return lastXp;
    }

    public void setLastXp(long lastXp) {
        this.lastXp = lastXp;
    }

    public void setBuyTime(long buyTime, ItemType itemType) {
        switch (itemType) {
            case NICKNAME:
                nickNameBuyTime = buyTime;
                break;
            case BOOSTER:
                boosterBuyTime = buyTime;
                break;
            case XPBOOSTER:
                xpBoosterBuyTime = buyTime;
                break;
            case DJ:
                djBuyTime = buyTime;
                break;
            case PREMIUM:
                premiumBuyTime = buyTime;
                break;
        }
    }

    public long getBuyTime(ItemType itemType) {
        switch (itemType) {
            case NICKNAME:
                return getNickNameBuyTime();
            case BOOSTER:
                return getBoosterBuyTime();
            case XPBOOSTER:
                return getXpBoosterBuyTime();
            case DJ:
                return getDjBuyTime();
            case PREMIUM:
                return getPremiumBuyTime();
            default:
                return 0;
        }
    }

    public long getPremiumBuyTime() {
        return premiumBuyTime;
    }

    public void setPremiumBuyTime(long premiumBuyTime) {
        this.premiumBuyTime = premiumBuyTime;
    }

    public long getBoosterBuyTime() {
        return boosterBuyTime;
    }

    public void setBoosterBuyTime(long boosterBuyTime) {
        this.boosterBuyTime = boosterBuyTime;
    }

    public long getDjBuyTime() {
        return djBuyTime;
    }

    public void setDjBuyTime(long djBuyTime) {
        this.djBuyTime = djBuyTime;
    }

    public long getNickNameBuyTime() {
        return nickNameBuyTime;
    }

    public void setNickNameBuyTime(long nickNameBuyTime) {
        this.nickNameBuyTime = nickNameBuyTime;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public Map<ItemType, Item> getItemStack() {
        return itemStack;
    }

    public void setItemStack(Map<ItemType, Item> itemStack) {
        this.itemStack = itemStack;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public int getStartXp() {
        return startXp;
    }

    public void setStartXp(int startXp) {
        this.startXp = startXp;
    }

    public int getStartCoins() {
        return startCoins;
    }

    public void setStartCoins(int startCoins) {
        this.startCoins = startCoins;
    }

    public int getMessages() {
        return messages;
    }

    public void setMessages(int messages) {
        this.messages = messages;
    }

    public boolean hasItem(ItemType itemType) {
        return itemStack.containsKey(itemType);
    }

    public boolean exists() {
        return id >= 0;
    }

    public int getEventPoints() {
        return eventPoints;
    }

    public void setEventPoints(int eventPoints) {
        this.eventPoints = eventPoints;
    }

    public long getXpBoosterBuyTime() {
        return xpBoosterBuyTime;
    }

    public void setXpBoosterBuyTime(long xpBoosterBuyTime) {
        this.xpBoosterBuyTime = xpBoosterBuyTime;
    }

    public int getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(int diamonds) {
        this.diamonds = diamonds;
    }

    public int getStartDiamonds() {
        return startDiamonds;
    }

    public void setStartDiamonds(int startDiamonds) {
        this.startDiamonds = startDiamonds;
    }

    public boolean isDaily() {
        return daily;
    }

    public void setDaily(boolean daily) {
        this.daily = daily;
    }
}
