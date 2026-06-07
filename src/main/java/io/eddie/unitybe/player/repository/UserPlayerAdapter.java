package io.eddie.unitybe.player.repository;

import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPlayerAdapter implements UserPlayerRepository {
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;


    @Override
    public User save(User user, Player player) {
        User savedUser = userRepository.save(user);
        playerRepository.save(player);
        return savedUser;
    }
//
//    @Override
//    public Player findPlayer(Long userId) {
//
//    }
}
