package io.eddie.unitybe.friend.dto;

import jakarta.validation.constraints.NotNull;

public record FriendRequestDto(
        @NotNull(message = "요청 상대의 아이디는 필수갑입니다")
        Long toUserId
) {
}
