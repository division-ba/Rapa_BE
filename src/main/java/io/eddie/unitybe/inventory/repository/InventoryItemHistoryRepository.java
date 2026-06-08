package io.eddie.unitybe.inventory.repository;

import io.eddie.unitybe.dashboard.dto.ActivityCountDto;
import io.eddie.unitybe.dashboard.dto.PopularItemDto;
import io.eddie.unitybe.inventory.domain.InventoryActivity;
import io.eddie.unitybe.inventory.domain.InventoryItemHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryItemHistoryRepository extends JpaRepository<InventoryItemHistory, Long> {

    // 활동(activity)별 이벤트 건수. group by 한 줄이 "집계"의 핵심. (대시보드/성능 측정 대상)
    @Query("""
            select new io.eddie.unitybe.dashboard.dto.ActivityCountDto(h.activity, count(h))
            from InventoryItemHistory h
            group by h.activity
            """)
    List<ActivityCountDto> countByActivity();

    // 인기 아이템: 들어온 수량(획득/구매) 합계 기준 내림차순. limit은 Pageable로.
    @Query("""
            select new io.eddie.unitybe.dashboard.dto.PopularItemDto(i.name, sum(h.quantity))
            from InventoryItemHistory h
            join h.inventoryItem ii
            join ii.item i
            where h.activity in :activities
            group by i.id, i.name
            order by sum(h.quantity) desc
            """)
    List<PopularItemDto> findPopularItems(List<InventoryActivity> activities, Pageable pageable);

    // 최근 활동 피드. fetch join으로 item까지 한 번에 (Thymeleaf 렌더 시 lazy 문제 방지).
    @Query("""
            select h
            from InventoryItemHistory h
            join fetch h.inventoryItem ii
            join fetch ii.item
            order by h.createdAt desc
            """)
    List<InventoryItemHistory> findRecentWithItem(Pageable pageable);
}
