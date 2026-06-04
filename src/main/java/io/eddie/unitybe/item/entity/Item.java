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

    @Column(name = "rid", nullable = false, unique = true, length = 50)
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

    public Item(String name, String rid, String description, int price, int sellPrice, ItemType type, ItemGrade grade) {
        this.name = name;
        this.rid = rid;
        this.description = description;
        this.price = price;
        this.sellPrice = sellPrice;
        this.type = type;
        this.grade = grade;
    }
}
