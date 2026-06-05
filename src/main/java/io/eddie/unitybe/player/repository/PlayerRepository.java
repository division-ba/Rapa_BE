package io.eddie.unitybe.player.repository;

import io.eddie.unitybe.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlayerRepository extends JpaRepository<Player,Long> {
    @Query("""
        select p
        from Player p
        left join User u
        on p.id = u.id  
    """)
    Player findByUserId(Long userId);
}
