package io.eddie.unitybe.player.service;

import io.eddie.unitybe.player.dto.UserDataResponseDto;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.player.repository.UserPlayerRepository;
import io.eddie.unitybe.user.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final UserPlayerRepository userPlayerRepository;


    public UserDataResponseDto getUserData(AuthUser authUser) {

        return null;
    }
}
