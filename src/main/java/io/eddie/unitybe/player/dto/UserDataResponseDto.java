package io.eddie.unitybe.player.dto;

import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;

import java.util.List;

public record UserDataResponseDto(
        UserAccountResponseDto account,
        UserProfileResponseDto profile,
        UserWalletResponseDto wallet,
        List<InventoryItemResponseDto> inventory,
        Long friendCount
) {
}
