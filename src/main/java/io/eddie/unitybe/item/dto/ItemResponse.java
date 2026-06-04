package io.eddie.unitybe.item.dto;

import io.eddie.unitybe.item.entity.Item;

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
