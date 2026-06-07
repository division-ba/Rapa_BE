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

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean equipped;

    @Column(nullable = false)
    private LocalDateTime acquiredAt;

    // 새 인벤토리 행을 만드는 정적 팩토리 메서드.
    // 생성자를 직접 열지 않고 이렇게 의도가 드러나는 이름의 메서드로 만들면 "어떤 상태로 태어나는지"가 명확해진다.
    // (처음 획득이므로 장착되지 않은 상태(equipped=false)로 시작)
    public static InventoryItem create(User user, Item item, int quantity, LocalDateTime acquiredAt) {
        return InventoryItem.builder()
                .user(user)
                .item(item)
                .quantity(quantity)
                .equipped(false)
                .acquiredAt(acquiredAt)
                .build();
    }

    // 서비스가 시키는 대로 엔티티가 "자기 상태를 스스로" 바꾼다. (수량 누적 후 최종 수량 반환)
    public int increaseQuantity(int amount) {
        this.quantity += amount;
        return this.quantity;
    }

    // 예전에 버려서 soft delete(deletedAt 기록) 된 행을 되살린다.
    // 사용자가 "복구 후 수량 재설정"을 택했으므로, 수량을 0으로 초기화한 뒤 서비스에서 이번 획득 수량만큼 다시 누적한다.
    public void restore(LocalDateTime acquiredAt) {
        setDeletedAt(null);     // 삭제 표시 해제 → 다시 조회/사용 가능
        this.quantity = 0;      // 남아있던 옛 수량은 버리고 0부터 다시 센다
        this.equipped = false;
        this.acquiredAt = acquiredAt;
    }

}
