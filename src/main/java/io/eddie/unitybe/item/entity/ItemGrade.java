package io.eddie.unitybe.item.entity;

// 아이템 등급. 일반 < 고급 < 희귀 < 영웅 < 전설.
public enum ItemGrade {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY;

    // 파일에서 들어온 문자열(대소문자 무관)을 enum 으로 변환. 일치하는 값이 없으면 null.
    public static ItemGrade fromString(String value) {
        for (ItemGrade itemGrade : values()) {
            if (itemGrade.name().equalsIgnoreCase(value)) {
                return itemGrade;
            }
        }
        return null;
    }
}
