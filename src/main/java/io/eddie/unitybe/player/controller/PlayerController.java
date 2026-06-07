package io.eddie.unitybe.player.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.player.dto.UserDataResponseDto;
import io.eddie.unitybe.player.dto.UserProfileResponseDto;
import io.eddie.unitybe.player.dto.UserWalletResponseDto;
import io.eddie.unitybe.player.service.PlayerService;
import io.eddie.unitybe.user.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    //전체 데이터 조회
    @GetMapping("/data")
    public ResponseEntity<ApiResponse<UserDataResponseDto>> getUserData(@AuthenticationPrincipal AuthUser authUser) {
        UserDataResponseDto responseDto = playerService.getUserData(authUser);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.OK);
    }

    //게임 프로필 데이터 조회
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getUserProfile(@AuthenticationPrincipal AuthUser authUser) {
        UserProfileResponseDto responseDto = playerService.getUserProfile(authUser);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.OK);
    }

    //지갑 조회
    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<UserWalletResponseDto>> getWallet(@AuthenticationPrincipal AuthUser authUser) {
        UserWalletResponseDto responseDto = playerService.getUserWallet(authUser);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.OK);
    }

}
