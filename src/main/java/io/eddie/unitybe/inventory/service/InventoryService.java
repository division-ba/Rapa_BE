package io.eddie.unitybe.inventory.service;

import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public List<InventoryItemResponseDto> getInventory(Long userId) {
        return inventoryItemRepository.findAllByUserIdAndDeletedAtIsNull(userId).stream()
                .map(InventoryItemResponseDto::from)
                .toList();
    }
}
