package io.eddie.unitybe.inventory.domain;

import io.eddie.unitybe.common.domain.HistoryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "inventory_item_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItemHistory extends HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryActivity activity;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int beforeQuantity;

    @Column(nullable = false)
    private int afterQuantity;

    public static InventoryItemHistory acquired(InventoryItem inventoryItem, int quantity, int beforeQuantity, int afterQuantity) {
        return InventoryItemHistory.builder()
                .inventoryItem(inventoryItem)
                .activity(InventoryActivity.ACQUIRED)
                .quantity(quantity)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .build();
    }

    public static InventoryItemHistory discarded(InventoryItem inventoryItem, int quantity, int beforeQuantity, int afterQuantity) {
        return InventoryItemHistory.builder()
                .inventoryItem(inventoryItem)
                .activity(InventoryActivity.DISCARD)
                .quantity(quantity)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .build();
    }

    public static InventoryItemHistory sold(InventoryItem inventoryItem, int quantity, int beforeQuantity, int afterQuantity) {
        return InventoryItemHistory.builder()
                .inventoryItem(inventoryItem)
                .activity(InventoryActivity.SALE)
                .quantity(quantity)
                .beforeQuantity(beforeQuantity)
                .afterQuantity(afterQuantity)
                .build();
    }
}
