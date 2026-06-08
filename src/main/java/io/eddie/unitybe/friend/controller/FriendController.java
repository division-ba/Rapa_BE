package io.eddie.unitybe.friend.controller;

import io.eddie.unitybe.common.dto.ApiResponse;
import io.eddie.unitybe.friend.dto.FriendRequestDto;
import io.eddie.unitybe.friend.dto.FriendRequestResponseDto;
import io.eddie.unitybe.friend.service.FriendService;
import io.eddie.unitybe.user.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<FriendRequestResponseDto>> requestFriend(@AuthenticationPrincipal AuthUser authUser,
                                                                               @RequestBody @Valid FriendRequestDto request) {
        FriendRequestResponseDto response = friendService.requestFriend(authUser, request);
        return new ResponseEntity<>(ApiResponse.success("친구 요청을 보냈습니다.",response), HttpStatus.OK);
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ApiResponse<FriendRequestResponseDto>> acceptRequest(@AuthenticationPrincipal AuthUser authUser,
                                                                               @PathVariable Long requestId) {
        FriendRequestResponseDto response = friendService.acceptRequest(authUser, requestId);
        return new ResponseEntity<>(ApiResponse.success("친구 요청을 수락했습니다.",response), HttpStatus.OK);
    }
}
