package io.eddie.unitybe.user.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.user.dto.SignUpRequestDto;
import io.eddie.unitybe.user.dto.SignUpResponseDto;
import io.eddie.unitybe.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignUpResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto requestDto) {
         SignUpResponseDto responseDto = userService.signup(requestDto);
         return new ResponseEntity<>(ApiResponse.success("가입되었습니다.",responseDto), HttpStatus.OK);
    }


}
