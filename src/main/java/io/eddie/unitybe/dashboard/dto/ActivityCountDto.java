package io.eddie.unitybe.dashboard.dto;

import io.eddie.unitybe.inventory.domain.InventoryActivity;

// 활동별 건수 (예: ACQUIRED 12건).
public record ActivityCountDto(
        InventoryActivity activity,
        Long count
) {
}
