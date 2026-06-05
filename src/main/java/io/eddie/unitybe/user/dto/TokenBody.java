package io.eddie.unitybe.user.dto;

import io.eddie.unitybe.user.domain.Role;

public record TokenBody(
        Long userId,
        String email,
        Role role
) {
}
