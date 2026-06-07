package io.eddie.unitybe.item.repository;

import io.eddie.unitybe.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Spring Data JPA : 메서드 이름 규칙만으로 쿼리를 자동 생성한다. (deletedAtIsNull = soft delete 안 된 것만)
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 삭제되지 않은 아이템 전체 조회.
    List<Item> findAllByDeletedAtIsNull();

    // 삭제되지 않은 아이템 단건 조회. 없으면 빈 Optional → 서비스에서 예외로 변환.
    Optional<Item> findByIdAndDeletedAtIsNull(Long id);
}
