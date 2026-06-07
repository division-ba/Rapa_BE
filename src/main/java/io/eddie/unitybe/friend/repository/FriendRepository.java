package io.eddie.unitybe.friend.repository;

import io.eddie.unitybe.friend.domain.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("""
        select count(f) > 0
        from Friend f
        where (f.fromUser.id = :user1Id and f.toUser.id = :user2Id)
             or (f.fromUser.id = :user2Id and f.toUser.id = :user1Id)
        """)
    boolean existsFriendBetween(Long user1Id, Long user2Id);

}
