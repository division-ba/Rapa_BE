package io.eddie.unitybe.player.repository;

import io.eddie.unitybe.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player,Long> {
    @Query("""
        select p
        from Player p
        join fetch p.user
        where p.user.id = :userId
    """)
    Optional<Player> findByUserIdWithUser(Long userId);
}
