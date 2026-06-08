package io.eddie.unitybe.player.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.friend.service.FriendService;
import io.eddie.unitybe.inventory.dto.InventoryItemResponseDto;
import io.eddie.unitybe.inventory.service.InventoryService;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.dto.UserDataResponseDto;
import io.eddie.unitybe.player.dto.UserProfileResponseDto;
import io.eddie.unitybe.player.dto.UserWalletResponseDto;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final InventoryService inventoryService;
    private final FriendService friendService;


    //유저 전체 데이터 조회
    @Transactional(readOnly = true)
    public UserDataResponseDto getUserData(AuthUser authUser) {
        log.info("PlayerService.getUserData : id ::: {}", authUser.getId());
        Player player = playerRepository.findByUserIdWithUser(authUser.getId())
                .orElseThrow(() -> new DiversionException(ErrorCode.PLAYER_NOT_FOUND));
        User user = player.getUser();
        UserAccountResponseDto account = UserAccountResponseDto.from(user);
        UserProfileResponseDto profile = new UserProfileResponseDto(player.getLevel(), player.getExp(), player.getTotalPlaySeconds());
        UserWalletResponseDto wallet = new UserWalletResponseDto(player.getGold(), player.getGem());
        List<InventoryItemResponseDto> inventory = inventoryService.getInventory(authUser.getId());
        Long countFriends = (long) friendService.countFriends(authUser.getId());

        return new UserDataResponseDto(account, profile, wallet,
                inventory, countFriends);
    }

    // 게임 캐릭터 프로필 조회
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(AuthUser authUser) {
        Player player = playerRepository.findById(authUser.getId())
                .orElseThrow(() -> new DiversionException(ErrorCode.PLAYER_NOT_FOUND));
        return new UserProfileResponseDto(player.getLevel(), player.getExp(), player.getTotalPlaySeconds());
    }

    //지갑 정보 조회
    @Transactional(readOnly = true)
    public UserWalletResponseDto getUserWallet(AuthUser authUser) {
        Player player = playerRepository.findById(authUser.getId())
                .orElseThrow(() -> new DiversionException(ErrorCode.PLAYER_NOT_FOUND));
        return new UserWalletResponseDto(player.getGold(), player.getGem());
    }
}
