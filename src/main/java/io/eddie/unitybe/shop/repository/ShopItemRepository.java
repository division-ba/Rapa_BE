package io.eddie.unitybe.shop.repository;

import io.eddie.unitybe.shop.domain.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    // NPC 상점의 진열 아이템(삭제 안 됨)을 정렬순서대로.
    List<ShopItem> findAllByNpcIdAndDeletedAtIsNullOrderBySortOrderAsc(Long npcId);
}
