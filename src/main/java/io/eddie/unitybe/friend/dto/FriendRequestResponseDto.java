package io.eddie.unitybe.friend.dto;

import io.eddie.unitybe.friend.domain.Friend;
import io.eddie.unitybe.friend.domain.FriendStatus;

import java.time.LocalDateTime;

public record FriendRequestResponseDto(
        Long friendRequestId,
        Long fromUserId,
        Long toUserId,
        FriendStatus status,
        LocalDateTime createdAt,
        String nickname
        ) {
    public static FriendRequestResponseDto from(Friend friend, String nickname) {
        return new FriendRequestResponseDto(
                friend.getId(), friend.getFromUser().getId(),
                friend.getToUser().getId(), friend.getStatus(),
                friend.getCreatedAt(), nickname);
    }
}
