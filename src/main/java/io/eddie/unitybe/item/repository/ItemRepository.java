package io.eddie.unitybe.item.repository;

import io.eddie.unitybe.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

//    soft delete 조회 필터가 빠졌어
//    지금 findAll(), findById()를 그냥 쓰는데, 이건 deleted_at 찍힌 행도 다 가져와. 우리가 "조회는 deleted_at IS NULL만" 정책 세웠잖아. 비유하면 폐기 도장 찍힌 서류까지 같이 꺼내오는 거지.
//    근데 — 아이템(도감)은 soft delete를 거의 안 하는 마스터 데이터라, 사실 item에선 이게 당장 문제는 아냐. 진짜 중요해지는 건 inventory야 (버린 아이템이 조회되면 안 되니까). 그래도 일관성 위해 item도 맞추려면:
    List<Item> findAllByDeletedAtIsNull();
    Optional<Item> findByIdAndDeletedAtIsNull(Long id);
}
