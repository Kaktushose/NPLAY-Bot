package de.kaktushose.levelbot.database.model;

import javax.persistence.*;

@Entity
@Table(name = "items")
public class Item {

    @Id
    private Integer itemId;
    private String name;
    private int price;
    private long duration;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "categoryId", referencedColumnName = "categoryId")
    private ItemCategory itemCategory;
    private boolean visible;

    public Item() {
    }

    public Item(int itemId, String name, int price, long duration, ItemCategory itemCategory, boolean visible) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.itemCategory = itemCategory;
        this.visible = visible;
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

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(ItemCategory itemCategory) {
        this.itemCategory = itemCategory;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
