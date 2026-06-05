package io.eddie.unitybe.user.dto;

import io.eddie.unitybe.user.domain.User;

public record UserProfileResponseDto(
        Long userId,
        String email,
        String nickname,
        String role,
        String status,
        String provider,
        String profileImage,
        String createdAt,
        String lastLoginAt
) {
    public static UserProfileResponseDto from (User user) {
        return new UserProfileResponseDto(user.getId(), user.getEmail(), user.getNickname(),
                user.getRole().name(), user.getStatus().name(), user.getProvider().name(),
                user.getProfileImageUrl(), user.getCreatedAt().toString(), user.getLastLoginAt()==null? null : user.getLastLoginAt().toString());
    }
}
