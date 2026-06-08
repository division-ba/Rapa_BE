package io.eddie.unitybe.shop.domain;

import io.eddie.unitybe.common.domain.BaseEntity;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.npc.domain.Npc;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// NPC 상점에 진열된 아이템(= npcItemId). NPC ↔ Item 연결 + 재고(quantity) + 정렬순서(sortOrder).
@Entity
@Table(name = "shop_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShopItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "npc_id", nullable = false)
    private Npc npc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Setter
    @Column(nullable = false)
    private int quantity;        // 재고 수량 (구매 시 차감)

    @Column(nullable = false)
    private Integer sortOrder;   // 진열 정렬 순서
}
