package io.eddie.unitybe.inventory.repository;

import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemHistoryRepository extends JpaRepository<InventoryItemHistory, Long> {
}
