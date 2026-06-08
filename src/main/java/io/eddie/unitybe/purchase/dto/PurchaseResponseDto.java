package io.eddie.unitybe.purchase.dto;

import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.player.domain.Player;

// 027 구매 응답: 구매 후 지갑 상태 + 획득한 인벤토리 아이템.
public record PurchaseResponseDto(
        WalletDto wallet,
        InventoryItemResponseDto acquiredItem
) {
    public record WalletDto(Long gold, Long gem) {
    }

    public static PurchaseResponseDto of(Player player, InventoryItem acquiredItem) {
        return new PurchaseResponseDto(
                new WalletDto(player.getGold(), player.getGem()),
                InventoryItemResponseDto.from(acquiredItem)
        );
    }
}
