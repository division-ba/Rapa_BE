package io.eddie.unitybe.user.dto;

import io.eddie.unitybe.user.domain.Role;

public record UserAuthInfo(
        Long id,
        String email,
        String password,
        Role role
) {
}
