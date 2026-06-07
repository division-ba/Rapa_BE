package io.eddie.unitybe.player.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.dto.UserDataResponseDto;
import io.eddie.unitybe.player.dto.UserProfileResponseDto;
import io.eddie.unitybe.player.dto.UserWalletResponseDto;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.player.repository.UserPlayerRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final UserPlayerRepository userPlayerRepository;


    //유저 전체 데이터 조회
    @Transactional(readOnly = true)
    public UserDataResponseDto getUserData(AuthUser authUser) {
        Player player = playerRepository.findByUserIdWithUser(authUser.getId())
                .orElseThrow(() -> new DiversionException(ErrorCode.NOT_FOUND_PLAYER));
        User user = player.getUser();
        UserAccountResponseDto account = UserAccountResponseDto.from(user);
        UserProfileResponseDto profile = new UserProfileResponseDto(player.getLevel(), player.getExp(), player.getTotalPlaySeconds());
        UserWalletResponseDto wallet = new UserWalletResponseDto(player.getGold(), player.getGem());

        return new UserDataResponseDto(account, profile, wallet,
                null, null);
    }
}
