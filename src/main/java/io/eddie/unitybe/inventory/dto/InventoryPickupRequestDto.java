package io.eddie.unitybe.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryPickupRequestDto(
        @NotNull(message = "아이템 ID는 필수입니다.")
        Long itemId,

        @NotNull(message = "아이템 수량은 필수입니다.")
        @Min(value = 1, message = "아이템 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
