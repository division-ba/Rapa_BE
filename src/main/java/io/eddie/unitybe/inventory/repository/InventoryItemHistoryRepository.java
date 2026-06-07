package io.eddie.unitybe.inventory.repository;

import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// 이력 저장 전용. JpaRepository 만 상속해도 save() 등 기본 CRUD 가 제공되므로,
// #27 에서는 "저장(save)"만 쓰면 되어 따로 선언할 메서드가 없다. (조회 기능은 이력 조회 이슈에서 추가)
public interface InventoryItemHistoryRepository extends JpaRepository<InventoryItemHistory, Long> {
}
