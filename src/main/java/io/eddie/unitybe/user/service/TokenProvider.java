package io.eddie.unitybe.user.service;

import io.eddie.unitybe.user.dto.KeyPair;
import io.eddie.unitybe.user.dto.TokenBody;
import io.eddie.unitybe.common.config.properties.JwtProperties;
import io.eddie.unitybe.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;

    //키페어
    public KeyPair issueKeyPair(Long id, String email, String nickname, Role role) {
        String accessToken = issueAccessToken(id, email, nickname, role);
        String refreshToken = issueRefreshToken(id, email, nickname, role);

        return new KeyPair(accessToken, refreshToken,
                jwtProperties.getValidations().getAccess()/1000);
    }
    // access & refresh 토큰 만들기
    public String issueAccessToken(Long id, String email, String nickname, Role role) {
        return issue(id, email, nickname, role, jwtProperties.getValidations().getAccess());
    }
    public String issueRefreshToken(Long id, String email, String nickname, Role role) {
            return issue(id, email, nickname, role, jwtProperties.getValidations().getRefresh());
        }

    //jwt 토큰 만들기
    private String issue(Long id, String email, String nickname, Role role, Long validTime) {
        return Jwts.builder()
                .subject(id.toString())
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("role",role.toString())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + validTime))
                .signWith(getSecretKey())
                .compact();

    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecrets().getAppKey().getBytes());
    }

    //토큰 검사
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch ( JwtException e ) {
            log.error("Token validation failed: {}", e.getMessage());
        } catch ( IllegalStateException e ) {
            log.error("Illegal state during token validation");
        } catch ( Exception e ) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
        }

        return false;
    }

    //토큰 상세값 파싱
    public Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
    }

    //토큰 파싱
    public TokenBody parseJwt(String token) {
        Jws<Claims> claims = parseClaims(token);

        String sub =  claims.getPayload().getSubject();
        String email = claims.getPayload().get("email", String.class);
        String nickname = claims.getPayload().get("nickname", String.class);
        Role role = claims.getPayload().get("role", Role.class);
        return new TokenBody(Long.parseLong(sub),email, nickname, role);
    }
}
