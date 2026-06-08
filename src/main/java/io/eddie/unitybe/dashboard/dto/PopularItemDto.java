package io.eddie.unitybe.dashboard.dto;

// 인기 아이템 (이름 + 들어온 수량 합계).
public record PopularItemDto(
        String itemName,
        Long totalQuantity
) {
}
