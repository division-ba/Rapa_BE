package io.eddie.unitybe.purchase.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.npc.repository.NpcRepository;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.purchase.domain.PurchaseHistory;
import io.eddie.unitybe.purchase.dto.PurchaseRequestDto;
import io.eddie.unitybe.purchase.dto.PurchaseResponseDto;
import io.eddie.unitybe.purchase.repository.PurchaseHistoryRepository;
import io.eddie.unitybe.shop.domain.ShopItem;
import io.eddie.unitybe.shop.repository.ShopItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final NpcRepository npcRepository;
    private final ShopItemRepository shopItemRepository;
    private final PlayerRepository playerRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final InventoryService inventoryService;

    // 027: NPC 상점 아이템 구매 — 재고 확인 → 골드 차감 → 인벤 추가(PURCHASE) → 구매 이력.
    @Transactional
    public PurchaseResponseDto purchase(Long userId, Long npcId, Long npcItemId, PurchaseRequestDto request) {
        int quantity = request.quantity();

        // 1) NPC + 상점 아이템 확인
        npcRepository.findByIdAndDeletedAtIsNull(npcId)
                .orElseThrow(() -> new DiversionException(ErrorCode.NPC_NOT_FOUND));
        ShopItem shopItem = shopItemRepository.findByIdAndNpcIdAndDeletedAtIsNull(npcItemId, npcId)
                .orElseThrow(() -> new DiversionException(ErrorCode.SHOP_ITEM_NOT_FOUND));

        // 2) 재고 확인
        if (shopItem.getQuantity() < quantity) {
            throw new DiversionException(ErrorCode.SHOP_ITEM_OUT_OF_STOCK);
        }

        // 3) 골드 확인 + 차감 (Player.id == user.id)
        Player player = playerRepository.findById(userId)
                .orElseThrow(() -> new DiversionException(ErrorCode.PLAYER_NOT_FOUND));
        long totalPrice = (long) shopItem.getItem().getPrice() * quantity;
        if (player.getGold() < totalPrice) {
            throw new DiversionException(ErrorCode.INSUFFICIENT_GOLD);
        }
        long beforeGold = player.getGold();
        player.setGold(beforeGold - totalPrice);
        long afterGold = player.getGold();

        // 4) 재고 차감
        shopItem.setQuantity(shopItem.getQuantity() - quantity);

        // 5) 인벤토리에 추가 (PURCHASE 이력)
        InventoryItem acquired = inventoryService.addPurchasedItem(userId, shopItem.getItem(), quantity);

        // 6) 구매 이력 저장
        purchaseHistoryRepository.save(
                PurchaseHistory.of(player.getUser(), shopItem, quantity, beforeGold, afterGold));

        return PurchaseResponseDto.of(player, acquired);
    }
}
