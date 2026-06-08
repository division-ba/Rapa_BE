package io.eddie.unitybe.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// 029 선물 요청 본문. targetPlayerId = 받을 상대 player_id(=user id).
public record GiftRequestDto(
        @NotNull(message = "받는 유저 ID는 필수입니다.")
        Long targetPlayerId,

        @NotNull(message = "선물 수량은 필수입니다.")
        @Min(value = 1, message = "선물 수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
