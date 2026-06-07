package io.eddie.unitybe.item.entity;

import io.eddie.unitybe.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게임 내 아이템의 "원본(마스터) 데이터" 테이블. 유저가 보유하는 것은 InventoryItem 이 이 Item 을 참조한다.
@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 가 내부적으로 쓰는 기본 생성자 (외부에서 못 쓰게 protected)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // 빌더가 쓰는 전체 필드 생성자 (외부 직접 호출은 막음)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 를 DB 의 auto_increment 에 위임
    private Long id;

    // rid : 클라이언트/기획에서 쓰는 고유 식별 문자열(resource id). unique 제약으로 중복 등록을 막는다.
    @Column(name = "rid", nullable = false, unique = true, length = 50)
    private String rid;

    @Column(nullable = false, length = 50)
    private String name;

    // @Enumerated(STRING) : enum 을 DB 에 숫자가 아닌 이름("WEAPON" 등)으로 저장 → 순서가 바뀌어도 안전.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemGrade grade;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private int price;      // 구매가

    @Column(nullable = false)
    private int sellPrice;  // 판매가

    // 빌더 외에, 파일 일괄 등록 시 쓰기 좋은 "필요한 값만 받는" 생성자. (id/감사필드는 JPA·DB 가 채운다)
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
