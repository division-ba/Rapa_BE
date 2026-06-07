package io.eddie.unitybe.inventory.domain;

import io.eddie.unitybe.common.domain.HistoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인벤토리 변동의 "장부(이력)" 테이블.
// 지금은 획득(ACQUIRED)만 남기지만, 추후 판매/선물 등도 같은 표에 종류(activity)만 다르게 적어 쌓는다.
// HistoryEntity 를 상속하므로 생성 시각(createdAt)이 자동으로 기록된다. (이력은 수정/삭제 대상이 아니라 createdAt 만 필요)
@Entity
@Table(name = "inventory_item_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemHistory extends HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 인벤토리 행에 대한 변동인지. LAZY = 실제로 쓸 때만 DB 에서 끌어온다(불필요한 조회 방지).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    // 변동 종류(획득/판매/선물 등). EnumType.STRING = DB 에 숫자가 아닌 "ACQUIRED" 같은 문자열로 저장 → 가독성/안전성↑.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryActivity activity;

    @Column(nullable = false)
    private int quantity;        // 이번에 변동된 수량

    @Column(nullable = false)
    private int beforeQuantity;  // 변동 전 수량

    @Column(nullable = false)
    private int afterQuantity;   // 변동 후 수량 (before/after 를 같이 남기면 나중에 추적·정산이 쉬워진다)

    // "획득" 이력을 만드는 정적 팩토리. activity 는 항상 ACQUIRED 로 고정해 실수를 막는다.
    public static InventoryItemHistory acquired(InventoryItem inventoryItem, int quantity, int beforeQuantity, int afterQuantity) {
        return InventoryItemHistory.builder()
                .inventoryItem(inventoryItem)
                .activity(InventoryActivity.ACQUIRED)
                .quantity(quantity)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .build();
    }
}
