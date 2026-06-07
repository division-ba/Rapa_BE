package io.eddie.unitybe.shop.dto;

import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.shop.domain.ShopItem;

// 025/026 응답의 상점 아이템(NpcShopItemResponse). npcItemId = ShopItem.id.
public record ShopItemResponseDto(
        Long npcItemId,
        Long itemId,
        String rId,
        String itemName,
        String itemType,
        String itemGrade,
        String description,
        Integer price,
        Integer sellPrice,
        Integer quantity,
        Integer sortOrder
) {
    public static ShopItemResponseDto from(ShopItem shopItem) {
        Item item = shopItem.getItem();
        return new ShopItemResponseDto(
                shopItem.getId(),
                item.getId(),
                item.getRid(),
                item.getName(),
                item.getType().name(),
                item.getGrade().name(),
                item.getDescription(),
                item.getPrice(),
                item.getSellPrice(),
                shopItem.getQuantity(),
                shopItem.getSortOrder()
        );
    }
}
