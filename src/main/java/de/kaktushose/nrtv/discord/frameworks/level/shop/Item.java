package de.kaktushose.nrtv.discord.frameworks.level.shop;


import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.TimeUnit;

public abstract class Item {

    protected int price, type;
    protected String name, description, error, success;
    protected long duration;
    protected ItemType itemType;

    public Item(int price, String name, String description, long duration, int type, ItemType itemType) {
        this.price = price;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.type = type;
        error = "unknown";
        success = "unknown";
        this.itemType = itemType;
    }

    public String getRemainingTimeAsDate(long buyTime) {
        if (duration < 1) {
            return "unbegrenzt";
        }
        long millis = duration - (System.currentTimeMillis() - buyTime);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        String daysPattern = days != 1 ?  "%d Tage" : "ein Tag";
        String hoursPattern = hours != 1 ? "%d Stunden" : "eine Stunde";
        return String.format(daysPattern, days) + " und " + String.format(hoursPattern, hours);
    }

    public long getRemainingTimeAsLong(long buyTime) {
        if (duration < 1) {
            return Long.MAX_VALUE;
        }
        return duration - (System.currentTimeMillis() - buyTime);
    }

    public abstract boolean validateTransaction(BotUser botUser);

    public abstract void buy(Bot bot, Member member);

    public abstract String getSuccessMessage();

    public abstract void onItemExpiration(Bot bot, Member member);

    public boolean isExpiring(long buyTime) {
        return (TimeUnit.MILLISECONDS.toHours(getRemainingTimeAsLong(buyTime)) < 24);
    }

    public boolean isExpired(long buyTime) {
        return getRemainingTimeAsLong(buyTime) < 0;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getErrorMessage() {
        return error;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getDuration() {
        return duration;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public String toString() {
        return name + ": " + description + " Dauer: " + String.format("%d Tag(e)", TimeUnit.MILLISECONDS.toDays(duration));
    }
}
