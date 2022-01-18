package de.kaktushose.levelbot.shop.data.items;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "items")
public class Item {

    @Id
    private Integer itemId;
    private String name;
    private int price;
    private long duration;
    private int categoryId;
    private boolean visible;
    private long roleId;

    public Item() {
    }

    public Item(int itemId, String name, int price, long duration, int categoryId, boolean visible, long roleId) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.categoryId = categoryId;
        this.visible = visible;
        this.roleId = roleId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRemainingTimeAsDate(long buyTime) {
        if (duration < 1) {
            return "unbegrenzt";
        }
        long millis = duration - (System.currentTimeMillis() - buyTime);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis));
        String daysPattern = days != 1 ? "%d Tage" : "ein Tag";
        String hoursPattern = hours != 1 ? "%d Stunden" : "eine Stunde";
        return String.format(daysPattern, days) + " und " + String.format(hoursPattern, hours);
    }

    public long getRemainingTimeAsLong(long buyTime) {
        return duration - (System.currentTimeMillis() - buyTime);
    }

}
