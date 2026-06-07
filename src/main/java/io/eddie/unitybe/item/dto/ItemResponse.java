package io.eddie.unitybe.item.dto;

import io.eddie.unitybe.item.entity.Item;

// 나가는 응답 상자. 엔티티를 그대로 노출하지 않고 화면에 필요한 값만 담는다.
// (enum 은 .name() 으로 문자열화해서 내려준다)
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

    // Item 엔티티 -> 응답 DTO 변환기.
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
