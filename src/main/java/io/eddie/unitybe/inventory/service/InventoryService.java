package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.inventory.domain.InventoryItem;
import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.dto.InventoryPickupRequestDto;
import io.eddie.unitybe.inventory.repository.InventoryItemHistoryRepository;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.item.repository.ItemRepository;
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

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new DiversionException(ErrorCode.INVALID_ITEM_QUANTITY);
        }
    }
}
