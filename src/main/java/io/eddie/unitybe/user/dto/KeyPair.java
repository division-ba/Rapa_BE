package io.eddie.unitybe.user.dto;

public record KeyPair(
        String accessToken,
        String refreshToken,
        Long accessExpiresInSeconds
) {

}
