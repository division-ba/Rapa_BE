package io.eddie.unitybe.player.domain;

import io.eddie.unitybe.common.domain.BaseEntity;
import io.eddie.unitybe.item.entity.Item;
import io.eddie.unitybe.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Table(name = "players")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Long exp;

    @Column(nullable = false)
    private Long totalPlaySeconds;

    @Setter
    @Column(nullable = false)
    private Long gold;

    @Column(nullable = false)
    private Long gem;

    @OneToMany
    private List<Item> inventory;  //todo: Inventory  추가시 InventoryItem으로 변경

    public Player(User user) {
        this.user = user;
        user.setPlayer(this);
        this.level = 1;
        this.exp = 0L;
        this.totalPlaySeconds = 0L;
        this.gold = 0L;
        this.gem = 0L;
    }



}
