package io.eddie.unitybe.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 027 구매 요청 본문.
public record PurchaseRequestDto(
        @NotNull(message = "구매 수량은 필수입니다.")
        @Min(value = 1, message = "구매 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
