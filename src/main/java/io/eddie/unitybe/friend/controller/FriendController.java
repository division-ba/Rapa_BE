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

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    // 친구 요청 생성
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<FriendRequestResponseDto>> requestFriend(@AuthenticationPrincipal AuthUser authUser,
                                                                               @RequestBody @Valid FriendRequestDto request) {
        FriendRequestResponseDto response = friendService.requestFriend(authUser, request);
        return new ResponseEntity<>(ApiResponse.success("친구 요청을 보냈습니다.",response), HttpStatus.OK);
    }

    //친구 요청 수락
    @PostMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<FriendRequestResponseDto>> acceptRequest(@AuthenticationPrincipal AuthUser authUser,
                                                                               @PathVariable Long requestId) {
        FriendRequestResponseDto response = friendService.acceptRequest(authUser, requestId);
        return new ResponseEntity<>(ApiResponse.success("친구 요청을 수락했습니다.",response), HttpStatus.OK);
    }

    //친구 요청 거절
    @PostMapping("/requests/{requestId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineRequest(@AuthenticationPrincipal AuthUser authUser,
                                                                               @PathVariable Long requestId) {
        friendService.declineRequest(authUser, requestId);
        return new ResponseEntity<>(ApiResponse.success("친구 요청을 거절했습니다."),HttpStatus.OK);
    }

    //친구 요청 취소
    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<Void>> canceledRequest(@AuthenticationPrincipal AuthUser authUser,
                                                            @PathVariable Long requestId) {
        friendService.canceledRequest(authUser, requestId);
        return new ResponseEntity<>(ApiResponse.success("친구 요청을 취소했습니다."),HttpStatus.OK);
    }

    // 받은 친구 요청 목록
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendRequestResponseDto>>> getRequestList(@AuthenticationPrincipal AuthUser authUser) {
        List<FriendRequestResponseDto> response = friendService.getRequestList(authUser);
        return new ResponseEntity<>(ApiResponse.success(response),HttpStatus.OK);
    }

    // 친구 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendRequestResponseDto>>> getFriendList(@AuthenticationPrincipal AuthUser authUser) {
        List<FriendRequestResponseDto> response = friendService.getFriendList(authUser);
        return new ResponseEntity<>(ApiResponse.success(response),HttpStatus.OK);
    }

    //친구 삭제
    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long friendUserId) {
        friendService.deleteFriend(authUser, friendUserId);
        return new ResponseEntity<>(ApiResponse.success("친구를 삭제했습니다."), HttpStatus.OK);
    }




}
