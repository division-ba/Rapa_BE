package io.eddie.unitybe.inventory.domain;

// 인벤토리 변동의 "종류". 이력(InventoryItemHistory)에 무슨 일이 있었는지 표시하는 꼬리표다.
// 이번 이슈(#27)는 획득만 다루므로 ACQUIRED 하나만 둔다.
// 판매/선물/사용 등은 각 기능 이슈에서 값을 추가하며 같은 장부에 함께 쌓을 예정이다.
public enum InventoryActivity {
    ACQUIRED
}
