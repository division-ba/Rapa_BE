package io.eddie.unitybe.inventory.repository;

import io.eddie.unitybe.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findAllByUserIdAndDeletedAtIsNull(Long userId);
}
