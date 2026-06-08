package io.eddie.unitybe.inventory.domain;

public enum InventoryActivity {
    ACQUIRED,        // 필드 획득(pickup)
    DISCARD,         // 버리기
    SALE,            // 판매
    GIFT_SENT,       // 선물 보냄
    GIFT_RECEIVED    // 선물 받음
}
