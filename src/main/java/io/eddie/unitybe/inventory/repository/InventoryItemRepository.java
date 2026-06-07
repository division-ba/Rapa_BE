package io.eddie.unitybe.inventory.repository;

import io.eddie.unitybe.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Spring Data JPA : 메서드 "이름"만 규칙대로 적으면 스프링이 그에 맞는 SQL 을 자동으로 만들어준다. (본문 작성 불필요)
// Optional<> 은 "있을 수도 없을 수도 있는 상자". 없으면 빈 상자가 와서 NPE 를 예방한다.
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // userId 가 같고 deletedAt 이 null 인(=안 버린) 행 전부 조회. → 인벤토리 목록 조회용.
    List<InventoryItem> findAllByUserIdAndDeletedAtIsNull(Long userId);

    // (userId, itemId) 한 쌍으로 보유분을 찾는다. deletedAt 조건이 "없는" 이유 :
    // soft delete 된 행도 같이 찾아와야 "재획득 시 복구"가 가능하기 때문. (유니크 제약이 (user,item) 한 행을 보장)
    Optional<InventoryItem> findByUserIdAndItemId(Long userId, Long itemId);
}
