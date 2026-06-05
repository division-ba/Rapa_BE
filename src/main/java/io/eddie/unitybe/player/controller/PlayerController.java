package io.eddie.unitybe.player.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.player.dto.UserDataResponseDto;
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

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<UserDataResponseDto>> getUserData(@AuthenticationPrincipal AuthUser authUser) {
        UserDataResponseDto responseDto = playerService.getUserData(authUser);
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.OK);
    }

}
