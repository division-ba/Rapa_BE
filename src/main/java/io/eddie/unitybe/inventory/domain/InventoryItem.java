package io.eddie.unitybe.inventory.domain;

import io.eddie.unitybe.common.domain.BaseEntity;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventory_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inventory_item_user_item",
                columnNames = {"user_id", "item_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Setter
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean equipped;

    @Setter
    @Column(nullable = false)
    private LocalDateTime acquiredAt;

    public static InventoryItem create(User user, Item item, int quantity, LocalDateTime acquiredAt) {
        return InventoryItem.builder()
                .user(user)
                .item(item)
                .quantity(quantity)
                .equipped(false)
                .acquiredAt(acquiredAt)
                .build();
    }

}
