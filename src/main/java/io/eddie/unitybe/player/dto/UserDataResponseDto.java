package io.eddie.unitybe.player.dto;

import io.eddie.unitybe.item.dto.ItemResponse;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;

import java.util.List;

public record UserDataResponseDto(
        UserAccountResponseDto account,
        UserProfileResponseDto profile,
        UserWalletResponseDto wallet,
        List<ItemResponse> inventory,
        Long friendCount
) {
}
