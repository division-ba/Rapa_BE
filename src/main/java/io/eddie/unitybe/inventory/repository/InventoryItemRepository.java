package io.eddie.unitybe.inventory.repository;

import io.eddie.unitybe.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findAllByUserIdAndDeletedAtIsNull(Long userId);

    // deletedAt 필터를 일부러 두지 않는다. (user_id, item_id) 유니크 제약상 행은 하나뿐이라,
    // soft delete 된 행도 찾아와 재획득 시 복구해야 중복 INSERT(제약 위반)를 피할 수 있다.
    Optional<InventoryItem> findByUserIdAndItemId(Long userId, Long itemId);

    // 버리기: 경로의 userItemId(=InventoryItem.id)로 "내" 보유 행(삭제 안 된)을 찾는다.
    Optional<InventoryItem> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
