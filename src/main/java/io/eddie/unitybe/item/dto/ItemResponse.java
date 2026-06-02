package io.eddie.unitybe.item.dto;

import io.eddie.unitybe.item.entity.Item;

// 008/009 응답 DTO. 엔티티 필드명과 다른 카멜케이스 계약(itemId, rId 등)을 명세서에 맞춤.
public record ItemResponse(
        Long itemId,
        String rId,
        String itemName,
        String itemType,
        String itemGrade,
        String description,
        Integer price,
        Integer sellPrice
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getRid(),
                item.getName(),
                item.getType().name(),
                item.getGrade().name(),
                item.getDescription(),
                item.getPrice(),
                item.getSellPrice()
        );
    }
}
