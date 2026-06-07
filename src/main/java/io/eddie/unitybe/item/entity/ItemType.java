package io.eddie.unitybe.item.entity;

// 아이템 종류. 무기/방어구/소비아이템/기타.
public enum ItemType {
    WEAPON, ARMOR, CONSUMABLE, ETC;

    // 파일에서 들어온 문자열(대소문자 무관)을 enum 으로 변환. 일치하는 값이 없으면 null.
    public static ItemType fromString(String value) {
        for (ItemType itemType : ItemType.values()) {
            if (itemType.name().equalsIgnoreCase(value)) {
                return itemType;
            }
        }
        return null;
    }
}
