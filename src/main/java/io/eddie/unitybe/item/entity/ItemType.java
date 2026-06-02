package io.eddie.unitybe.item.entity;

public enum ItemType {
    WEAPON, ARMOR, CONSUMABLE, ETC;

    public static ItemType fromString(String value) {
        for (ItemType itemType : ItemType.values()) {
            if (itemType.name().equalsIgnoreCase(value)) {
                return itemType;
            }
        }
        return null;
    }
}
