package io.eddie.unitybe.purchase.repository;

import io.eddie.unitybe.purchase.domain.PurchaseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory, Long> {

    // 총 구매액(골드) = 구매마다 (차감 전 - 차감 후) 합. 데이터 없으면 0. (대시보드 카드)
    @Query("select coalesce(sum(p.beforeGold - p.afterGold), 0) from PurchaseHistory p")
    long totalGoldSpent();
}
