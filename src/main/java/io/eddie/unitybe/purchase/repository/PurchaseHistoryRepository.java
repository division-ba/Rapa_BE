package io.eddie.unitybe.purchase.repository;

import io.eddie.unitybe.purchase.domain.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {
}
