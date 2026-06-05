package io.eddie.unitybe.user.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.KeyPair;
import io.eddie.unitybe.user.dto.LogInRequestDto;
import io.eddie.unitybe.user.dto.RefreshRequestDto;
import io.eddie.unitybe.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    //로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<KeyPair>> login(@RequestBody @Valid LogInRequestDto request) {
        KeyPair keypair = userService.login(request);
        return new ResponseEntity<>(ApiResponse.success("로그인되었습니다.",  keypair), HttpStatus.OK);
    }

    //토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<KeyPair>> refresh(@RequestBody @Valid RefreshRequestDto request) {
        KeyPair keypair = userService.refresh(request);
        return new ResponseEntity<>(ApiResponse.success(keypair), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal AuthUser user) {
        userService.logout(user);
        return new ResponseEntity<>(ApiResponse.success("로그아웃되었습니다."), HttpStatus.OK);
    }
}
