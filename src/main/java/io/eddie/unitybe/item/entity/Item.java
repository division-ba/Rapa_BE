package io.eddie.unitybe.item.entity;

import io.eddie.unitybe.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유니티 리소스 ID (에셋 연결 키). 도감 상 유일값.
    @Column(nullable = false, unique = true, length = 50)
    private String rid;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemGrade grade;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int sellPrice;

    //    엔티티는 int price(기본형), DTO는 Integer price(객체형)야. ItemResponse.from에서 item.getPrice()(int) → Integer로 들어갈 때 자동 박싱돼서 동작은 해. 문제 없어.
    //    다만 알아둘 점: int는 null이 안 되니 "가격 미정"을 null로 표현 못 해. 가격은 항상 있는 값이니 int로 둔 거 합리적이야. 그대로 둬도 돼.

    public Item(Long id, String name, String rid, String description, int price, int sellPrice, ItemType type, ItemGrade grade) {
        this.id = id;
        this.name = name;
        this.rid = rid;
        this.description = description;
        this.price = price;
        this.sellPrice = sellPrice;
        this.type = type;
        this.grade = grade;
    }
}
