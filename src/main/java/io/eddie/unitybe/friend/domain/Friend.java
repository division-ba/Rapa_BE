package io.eddie.unitybe.friend.domain;

import io.eddie.unitybe.common.domain.BaseEntity;
import io.eddie.unitybe.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "friends")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friend extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FriendStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id",  nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id",  nullable = false)
    private User toUser;

    public Friend (User fromUser, User toUser) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.status = FriendStatus.PENDING;
    }

    public void delete() {
        this.status = FriendStatus.DELETED;
        setDeletedAt(LocalDateTime.now());
    }



}
