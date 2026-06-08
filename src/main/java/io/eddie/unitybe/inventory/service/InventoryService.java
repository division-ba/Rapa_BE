package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import io.eddie.unitybe.inventory.dto.GiftRequestDto;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.dto.InventoryPickupRequestDto;
import io.eddie.unitybe.inventory.dto.SellRequestDto;
import io.eddie.unitybe.inventory.repository.InventoryItemHistoryRepository;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.repository.ItemRepository;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryItemHistoryRepository inventoryItemHistoryRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    public List<InventoryItemResponseDto> getInventory(Long userId) {
        return inventoryItemRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(InventoryItemResponseDto::from)
                .toList();
    }

    @Transactional
    public InventoryItemResponseDto pickup(Long userId, InventoryPickupRequestDto request) {
        validateQuantity(request.quantity());

        Item item = itemRepository.findByIdAndDeletedAtIsNull(request.itemId())
                .orElseThrow(() -> new DiversionException(ErrorCode.ITEM_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();

        InventoryItem inventoryItem = inventoryItemRepository.findByUserIdAndItemId(userId, item.getId())
                .map(existing -> accumulate(existing, request.quantity(), now))
                .orElseGet(() -> create(userId, item, request.quantity(), now));

        return InventoryItemResponseDto.from(inventoryItem);
    }

    private InventoryItem accumulate(InventoryItem inventoryItem, int quantity, LocalDateTime now) {
        // soft delete 된 행을 재획득한 경우: 복구하면서 수량을 0부터 다시 센다. (복구 시 수량 재설정 정책)
        if (inventoryItem.getDeletedAt() != null) {
            inventoryItem.setDeletedAt(null);
            inventoryItem.setQuantity(0);
            inventoryItem.setAcquiredAt(now);
        }
        int before = inventoryItem.getQuantity();
        int after = before + quantity;
        inventoryItem.setQuantity(after);
        inventoryItemHistoryRepository.save(InventoryItemHistory.acquired(inventoryItem, quantity, before, after));
        return inventoryItem;
    }

    private InventoryItem create(Long userId, Item item, int quantity, LocalDateTime now) {
        // 인증된 요청이라 user 존재는 보장됨 → 조회 없이 FK용 프록시만 얻어 불필요한 SELECT를 아낀다.
        User user = userRepository.getReferenceById(userId);
        InventoryItem inventoryItem = inventoryItemRepository.save(InventoryItem.create(user, item, quantity, now));
        inventoryItemHistoryRepository.save(InventoryItemHistory.acquired(inventoryItem, quantity, 0, quantity));
        return inventoryItem;
    }

    // 022: 아이템 버리기 — 수량 차감, 0이면 soft delete. DISCARD 이력. (userItemId = InventoryItem.id)
    @Transactional
    public void discard(Long userId, Long userItemId, int quantity) {
        validateQuantity(quantity);

        InventoryItem inventoryItem = inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(userItemId, userId)
                .orElseThrow(() -> new DiversionException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        if (inventoryItem.getQuantity() < quantity) {
            throw new DiversionException(ErrorCode.INSUFFICIENT_ITEM_QUANTITY);
        }

        int before = inventoryItem.getQuantity();
        int after = before - quantity;
        inventoryItem.setQuantity(after);
        inventoryItemHistoryRepository.save(InventoryItemHistory.discarded(inventoryItem, quantity, before, after));

        // 다 버려서 0이 되면 soft delete (조회에서 제외)
        if (after == 0) {
            inventoryItem.setDeletedAt(LocalDateTime.now());
        }
    }

    // 028: 아이템 판매 — sellPrice×수량 만큼 골드 증가 + 수량 차감(0이면 soft delete). SALE 이력.
    @Transactional
    public void sell(Long userId, Long userItemId, SellRequestDto request) {
        validateQuantity(request.quantity());

        InventoryItem inventoryItem = inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(userItemId, userId)
                .orElseThrow(() -> new DiversionException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));

        if (inventoryItem.getQuantity() < request.quantity()) {
            throw new DiversionException(ErrorCode.INSUFFICIENT_ITEM_QUANTITY);
        }

        // 판매가 × 수량 만큼 골드 증가 (Player.id == user.id)
        Player player = playerRepository.findById(userId)
                .orElseThrow(() -> new DiversionException(ErrorCode.PLAYER_NOT_FOUND));
        long gain = (long) inventoryItem.getItem().getSellPrice() * request.quantity();
        player.setGold(player.getGold() + gain);

        int before = inventoryItem.getQuantity();
        int after = before - request.quantity();
        inventoryItem.setQuantity(after);
        inventoryItemHistoryRepository.save(InventoryItemHistory.sold(inventoryItem, request.quantity(), before, after));

        // 다 팔아서 0이 되면 soft delete (조회에서 제외)
        if (after == 0) {
            inventoryItem.setDeletedAt(LocalDateTime.now());
        }
    }

    // 029: 아이템 선물 — 내 수량 차감(GIFT_SENT) + 상대 인벤 누적(GIFT_RECEIVED). targetPlayerId = 상대 user/player id.
    @Transactional
    public void gift(Long userId, Long userItemId, GiftRequestDto request) {
        validateQuantity(request.quantity());

        if (userId.equals(request.targetPlayerId())) {
            throw new DiversionException(ErrorCode.SELF_GIFT_NOT_ALLOWED);
        }

        InventoryItem senderItem = inventoryItemRepository.findByIdAndUserIdAndDeletedAtIsNull(userItemId, userId)
                .orElseThrow(() -> new DiversionException(ErrorCode.INVENTORY_ITEM_NOT_FOUND));
        if (senderItem.getQuantity() < request.quantity()) {
            throw new DiversionException(ErrorCode.INSUFFICIENT_ITEM_QUANTITY);
        }

        User target = userRepository.findById(request.targetPlayerId())
                .orElseThrow(() -> new DiversionException(ErrorCode.TARGET_USER_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();

        // 보내는 쪽: 차감 + GIFT_SENT(상대=받는이)
        int before = senderItem.getQuantity();
        int after = before - request.quantity();
        senderItem.setQuantity(after);
        inventoryItemHistoryRepository.save(InventoryItemHistory.giftSent(senderItem, request.quantity(), before, after, target));
        if (after == 0) {
            senderItem.setDeletedAt(now);
        }

        // 받는 쪽: 누적/복구/신규 + GIFT_RECEIVED(상대=보낸이)
        User me = userRepository.getReferenceById(userId);
        receiveGift(request.targetPlayerId(), senderItem.getItem(), request.quantity(), me, now);
    }

    // 상대 인벤토리에 선물 아이템을 더한다(신규 생성/기존 누적/soft delete 복구) + GIFT_RECEIVED 이력.
    private void receiveGift(Long targetUserId, Item item, int quantity, User sender, LocalDateTime now) {
        InventoryItem receiverItem = findOrCreateInventoryItem(targetUserId, item, now);
        int before = receiverItem.getQuantity();
        int after = before + quantity;
        receiverItem.setQuantity(after);
        inventoryItemHistoryRepository.save(InventoryItemHistory.giftReceived(receiverItem, quantity, before, after, sender));
    }

    // 027: 구매한 아이템을 인벤토리에 더한다(누적/복구/신규) + PURCHASE 이력. PurchaseService에서 호출.
    @Transactional
    public InventoryItem addPurchasedItem(Long userId, Item item, int quantity) {
        InventoryItem inventoryItem = findOrCreateInventoryItem(userId, item, LocalDateTime.now());
        int before = inventoryItem.getQuantity();
        int after = before + quantity;
        inventoryItem.setQuantity(after);
        inventoryItemHistoryRepository.save(InventoryItemHistory.purchased(inventoryItem, quantity, before, after));
        return inventoryItem;
    }

    // 보유 행 조회(누적), soft delete면 복구, 없으면 신규 생성. 선물수령·구매 공용.
    private InventoryItem findOrCreateInventoryItem(Long userId, Item item, LocalDateTime now) {
        return inventoryItemRepository.findByUserIdAndItemId(userId, item.getId())
                .map(existing -> {
                    if (existing.getDeletedAt() != null) {
                        existing.setDeletedAt(null);
                        existing.setQuantity(0);
                        existing.setAcquiredAt(now);
                    }
                    return existing;
                })
                .orElseGet(() -> inventoryItemRepository.save(
                        InventoryItem.create(userRepository.getReferenceById(userId), item, 0, now)));
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new DiversionException(ErrorCode.INVALID_ITEM_QUANTITY);
        }
    }
}
