package io.eddie.unitybe.user.dto;
import io.eddie.unitybe.user.domain.Status;
import io.eddie.unitybe.user.domain.Provider;
import io.eddie.unitybe.user.domain.Role;
import io.eddie.unitybe.user.domain.User;

import java.time.LocalDateTime;

public record SignUpResponseDto(
        Long userId,
        String email,
        String nickname,
        Role role,
        Status status,
        Provider provider,
        String profileImageUrl,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    public SignUpResponseDto(User user) {
        this(user.getId(), user.getEmail(), user.getNickname(), user.getRole(), user.getStatus(),
                user.getProvider(), user.getProfileImageUrl(), user.getCreatedAt(), user.getLastLoginAt());
    }
}
