package io.eddie.unitybe.purchase.domain;

import io.eddie.unitybe.common.domain.HistoryEntity;
import io.eddie.unitybe.shop.domain.ShopItem;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 상점 구매 이력(장부). 구매 시점의 골드 before/after를 남긴다. HistoryEntity → createdAt 자동.
@Entity
@Table(name = "purchase_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PurchaseHistory extends HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_item_id", nullable = false)
    private ShopItem shopItem;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long beforeGold;

    @Column(nullable = false)
    private long afterGold;

    public static PurchaseHistory of(User user, ShopItem shopItem, int quantity, long beforeGold, long afterGold) {
        return PurchaseHistory.builder()
                .user(user)
                .shopItem(shopItem)
                .quantity(quantity)
                .beforeGold(beforeGold)
                .afterGold(afterGold)
                .build();
    }
}
