package io.eddie.unitybe.user.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.repository.UserPlayerRepository;
import io.eddie.unitybe.user.domain.RefreshToken;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.*;
import io.eddie.unitybe.user.repository.RefreshTokenRepository;
import io.eddie.unitybe.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final UserPlayerRepository userPlayerRepository;

    @Transactional
    public SignUpResponseDto signup(@Valid SignUpRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())) throw new DiversionException(ErrorCode.EXIST_EMAIL);
        User user = new User(requestDto.email(),
                passwordEncoder.encode(requestDto.password()),
                requestDto.nickname());
        Player player = new Player(user);
        User savedUser = userPlayerRepository.save(user,  player);
        return new SignUpResponseDto(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuthInfo user = userRepository.findAuthInfoByEmail(email).orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND_BY_EMAIL));
        return new AuthUser(user.id(), user.email(), user.password(), user.role().name());
    }


    //로그인
    @Transactional
    public KeyPair login(LogInRequestDto request) {
        //이메일 확인
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new DiversionException(ErrorCode.LOGIN_NOT_MACH));
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword()))
            throw new DiversionException(ErrorCode.LOGIN_NOT_MACH);
        user.setLastLoginAt(LocalDateTime.now());
        // 토큰 발급
        KeyPair keyPair = tokenProvider.issueKeyPair(user.getId(), user.getEmail(), user.getRole());

        //refresh토큰 유효시간 추출
        Date expiration = tokenProvider.parseExpiration(keyPair.refreshToken());
        LocalDateTime expirationTime =
                expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        // refresh토큰 db 저장
        refreshRepository.save(new RefreshToken(keyPair.refreshToken(),expirationTime, user));
        return keyPair;
    }

    // 토큰 갱신
    @Transactional
    public KeyPair refresh(RefreshRequestDto request) {
        //리프레쉬 토큰 확인
        RefreshToken refreshToken = refreshRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(() -> new DiversionException(ErrorCode.INVALID_REFRESH_TOKEN));

        // 유효시간이 지났으면 유호하지 않은 토큰
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new DiversionException(ErrorCode.INVALID_REFRESH_TOKEN);

        // 유효하다면 AccessToken/RefreshToken 다시 생성
        User user = refreshToken.getUser();
        KeyPair keyPair = tokenProvider.issueKeyPair(user.getId(), user.getEmail(), user.getRole());

        //refresh토큰 유효시간 추출
        Date expiration = tokenProvider.parseExpiration(keyPair.refreshToken());
        LocalDateTime expirationTime =
                expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

        refreshToken.update(keyPair.refreshToken(), expirationTime);

        // 반환
        return keyPair;
    }

    // 로그아웃
    @Transactional
    public void logout(AuthUser authUser) {
        // 유저 찾기
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND));

        // 해당 유저의 refresh 토큰 모두 삭제
        List<RefreshToken> refreshTokens = refreshRepository.findByUserId(user.getId());
        refreshRepository.deleteAll(refreshTokens);
    }

    //내 계정 정보 조회
    public UserAccountResponseDto getMyProfile(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND));
        return UserAccountResponseDto.from(user);
    }

    //다른 도메인에서 호출하는 메서드
    // user를 가져와야 할때 사용
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND));
    }
    //해당 user가 있는지 확인만 할때 사용
    public void checkUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND));
    }
}
