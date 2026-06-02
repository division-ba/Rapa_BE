package io.eddie.unitybe.item.dto;

public record ItemInputRequest(
        Long id,
        String name,
        String rId,
        String description,
        Integer price,
        Integer sellPrice,
        String type,
        String grade
) {
}
