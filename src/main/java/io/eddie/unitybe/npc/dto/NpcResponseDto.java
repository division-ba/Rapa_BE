package io.eddie.unitybe.npc.dto;

import io.eddie.unitybe.npc.domain.Npc;
import io.eddie.unitybe.shop.domain.ShopItem;
import io.eddie.unitybe.shop.dto.ShopItemResponseDto;

import java.util.List;

// 025/026 응답의 NPC + 상점 아이템 목록.
public record NpcResponseDto(
        Long npcId,
        String rId,
        String name,
        String description,
        String locationKey,
        Boolean active,
        List<ShopItemResponseDto> shopItems
) {
    public static NpcResponseDto from(Npc npc, List<ShopItem> shopItems) {
        return new NpcResponseDto(
                npc.getId(),
                npc.getRid(),
                npc.getName(),
                npc.getDescription(),
                npc.getLocationKey(),
                npc.isActive(),
                shopItems.stream().map(ShopItemResponseDto::from).toList()
        );
    }
}
