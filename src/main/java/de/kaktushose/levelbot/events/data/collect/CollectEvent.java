package de.kaktushose.levelbot.events.data.collect;

import de.kaktushose.levelbot.shop.data.items.Item;

import javax.persistence.*;

@Table(name = "collect_events")
@Entity
public class CollectEvent {

    @Id
    private Integer eventId;
    private String name;
    private String currencyName;
    private String currencyEmote;
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId")
    private Item item;
    private long roleId;
    private int itemBound;
    private int roleBound;

    public CollectEvent() {
    }

    public CollectEvent(Integer eventId,
                        String name,
                        String currencyName,
                        String currencyEmote,
                        Item item,
                        long roleId,
                        int itemBound,
                        int roleBound) {
        this.eventId = eventId;
        this.name = name;
        this.currencyName = currencyName;
        this.currencyEmote = currencyEmote;
        this.item = item;
        this.roleId = roleId;
        this.itemBound = itemBound;
        this.roleBound = roleBound;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyEmote() {
        return currencyEmote;
    }

    public void setCurrencyEmote(String currencyEmote) {
        this.currencyEmote = currencyEmote;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getItemBound() {
        return itemBound;
    }

    public void setItemBound(int itemBound) {
        this.itemBound = itemBound;
    }

    public int getRoleBound() {
        return roleBound;
    }

    public void setRoleBound(int roleBound) {
        this.roleBound = roleBound;
    }
}
