package io.eddie.unitybe.inventory.dto;

import io.eddie.unitybe.inventory.domain.InventoryItem;

import java.time.LocalDateTime;

public record InventoryItemResponseDto(
        Long userItemId,
        Long itemId,
        String rId,
        String itemName,
        String itemType,
        String itemGrade,
        String description,
        Integer price,
        Integer sellPrice,
        Integer quantity,
        Boolean equipped,
        LocalDateTime acquiredAt
) {
    public static InventoryItemResponseDto from(InventoryItem inventoryItem) {
        return new InventoryItemResponseDto(
                inventoryItem.getId(),
                inventoryItem.getItem().getId(),
                inventoryItem.getItem().getRid(),
                inventoryItem.getItem().getName(),
                inventoryItem.getItem().getType().name(),
                inventoryItem.getItem().getGrade().name(),
                inventoryItem.getItem().getDescription(),
                inventoryItem.getItem().getPrice(),
                inventoryItem.getItem().getSellPrice(),
                inventoryItem.getQuantity(),
                inventoryItem.isEquipped(),
                inventoryItem.getAcquiredAt()
        );
    }
}
