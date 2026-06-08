package io.eddie.unitybe.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 028 판매 요청 본문.
public record SellRequestDto(
        @NotNull(message = "판매 수량은 필수입니다.")
        @Min(value = 1, message = "판매 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
