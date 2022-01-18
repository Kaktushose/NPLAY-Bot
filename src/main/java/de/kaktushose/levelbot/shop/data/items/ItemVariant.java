package de.kaktushose.levelbot.shop.data.items;

public enum ItemVariant {
    LIGHT(0),
    BASIC(1),
    GOLD(2),
    UNLIMITED(3);

    private final int itemIdSummand;

    ItemVariant(int itemIdSummand) {
        this.itemIdSummand = itemIdSummand;
    }

    int getItemIdSummand() {
        return itemIdSummand;
    }
}
