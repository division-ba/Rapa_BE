package io.eddie.unitybe.item.entity;

public enum ItemGrade {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;
    public static ItemGrade fromString(String value) {
        for (ItemGrade itemGrade : values()) {
            if (itemGrade.name().equalsIgnoreCase(value)) {
                return itemGrade;
            }
        }
        return null;
    }
}
