package io.eddie.unitybe.player.repository;

import io.eddie.unitybe.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player,Long> {
}
