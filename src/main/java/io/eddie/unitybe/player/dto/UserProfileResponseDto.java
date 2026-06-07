package io.eddie.unitybe.player.dto;

public record UserProfileResponseDto(
        Integer level,
        Long exp,
        Long totalPlaySeconds
) {
}
