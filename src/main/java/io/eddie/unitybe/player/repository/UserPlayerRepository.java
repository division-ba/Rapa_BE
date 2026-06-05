package io.eddie.unitybe.player.repository;

import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.user.domain.User;

public interface UserPlayerRepository {
    User save(User user, Player player);
//    Player findPlayer(Long userId);
}
