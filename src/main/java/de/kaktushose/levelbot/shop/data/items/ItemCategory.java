package de.kaktushose.levelbot.shop.data.items;

public enum ItemCategory {

    PREMIUM(0, 0),
    DJ_PERK(1, 4),
    NICKNAME_PERK(2, 7),
    COIN_BOOSTER(3, 10),
    XP_BOOSTER(4, 12);

    final int categoryId;
    final int itemIdBaseValue;

    ItemCategory(int categoryId, int itemIdBaseValue) {
        this.categoryId = categoryId;
        this.itemIdBaseValue = itemIdBaseValue;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getItemId(ItemVariant variant) {
        return itemIdBaseValue + variant.getItemIdSummand();
    }


}
