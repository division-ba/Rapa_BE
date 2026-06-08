package io.eddie.unitybe.dashboard.dto;

import io.eddie.unitybe.inventory.domain.InventoryActivity;
import io.eddie.unitybe.inventory.domain.InventoryItemHistory;

import java.time.LocalDateTime;

// 최근 활동 피드 한 줄.
public record RecentActivityDto(
        LocalDateTime createdAt,
        InventoryActivity activity,
        String itemName,
        int quantity,
        int beforeQuantity,
        int afterQuantity
) {
    // 엔티티 → 피드 DTO (서비스 트랜잭션 안에서 변환해 lazy 문제 방지).
    public static RecentActivityDto from(InventoryItemHistory h) {
        return new RecentActivityDto(
                h.getCreatedAt(),
                h.getActivity(),
                h.getInventoryItem().getItem().getName(),
                h.getQuantity(),
                h.getBeforeQuantity(),
                h.getAfterQuantity()
        );
    }
}
