package io.eddie.unitybe.friend.repository;

import io.eddie.unitybe.friend.domain.Friend;
import io.eddie.unitybe.friend.domain.FriendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("""
        select count(f) > 0
        from Friend f
        where (f.fromUser.id = :user1Id and f.toUser.id = :user2Id)
             or (f.fromUser.id = :user2Id and f.toUser.id = :user1Id)
        """)
    boolean existsFriendBetween(Long user1Id, Long user2Id);

    @Query("""
        select f
        from Friend f
        join fetch f.fromUser
        join fetch f.toUser
        where f.toUser.id = :toUserId
            and f.status = :status
        """)
    List<Friend> findAllByToUserIdAndStatus(Long toUserId,  FriendStatus status);

    @Query("""
        select f
        from Friend f
        join fetch f.fromUser
        join fetch f.toUser
        where (f.fromUser.id = :userId or f.toUser.id = :userId)
            and f.status = :status
    """)
    List<Friend> findFriendList(Long userId, FriendStatus status);

    @Query("""
        select f
        from Friend f
        join fetch f.fromUser
        join fetch f.toUser
        where ((f.fromUser.id = :myId and f.toUser.id = :userId)
            or (f.toUser.id = :myId and f.fromUser.id = :userId))
        and f.status = :status
    """)
    Optional<Friend> findMyFriend(@Param("myId") Long myId,
                                  @Param("userId") Long userId,
                                  FriendStatus status);

}
