package io.eddie.unitybe.user.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.user.domain.RefreshToken;
import io.eddie.unitybe.user.domain.Role;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.*;
import io.eddie.unitybe.user.repository.RefreshTokenRepository;
import io.eddie.unitybe.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    TokenProvider tokenProvider;



    @Nested
    @DisplayName("signup 메서드는")
    public class Signup {

        String email;
        String password;
        String encodedPassword;
        String nickname;

        SignUpRequestDto requestDto;
        User user;
        User savedUser;

        @BeforeEach
        void setUp() {
            email = "newgamer@test.com";
            password = "mypassword123";
            encodedPassword = "encodedpassword123";
            nickname = "새싹게이머";

            requestDto = new SignUpRequestDto(email, password, nickname);
            user = new User(email, encodedPassword, nickname);
            savedUser = new User(1L, email, encodedPassword, nickname);
        }

        @Nested
        @DisplayName("유효한 입력이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("해당 유저를 저장하고 반환한다")
            void it_return_saved_user() {
                //given
                given(userRepository.existsByEmail(email)).willReturn(Boolean.FALSE);
                given(passwordEncoder.encode(any(String.class))).willReturn(encodedPassword);
                given(userRepository.save(any(User.class))).willReturn(savedUser);

                //when
                SignUpResponseDto responseDto = userService.signup(requestDto);

                //then
                Assertions.assertNotNull(responseDto);
                assertThat(responseDto.email()).isEqualTo(email);
                assertThat(responseDto.nickname()).isEqualTo(nickname);
            }

        }

        @Nested
        @DisplayName("중복된 이메일이 들어오면")
        class Context_with_duplicate_email {

            @Test
            @DisplayName("중복된 이메일의 오류가 발생한다")
            void it_throws_exist_email() {
                //given
                given(userRepository.existsByEmail(email)).willReturn(Boolean.TRUE);

                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> userService.signup(requestDto)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.EXIST_EMAIL.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("login메서드는")
    public class Login {
        String email;
        String password;
        String encodedPassword;
        String nickname;

        LogInRequestDto requestDto;
        User savedUser;
        KeyPair keyPair;

        @BeforeEach
        void setUp() {
            email = "newgamer@test.com";
            password = "mypassword123";
            encodedPassword = "encodedpassword123";
            nickname = "새싹게이머";

            requestDto = new LogInRequestDto(email, password);
            savedUser = new User(1L, email, encodedPassword, nickname);
            keyPair = new KeyPair(
                    "access-token",
                    "refresh-token",
                    900L
            );

        }

        @Nested
        @DisplayName("유효한 입력이 주어지면")
        class Context_with_valid_request {
            @Test
            @DisplayName("토큰 페어를 반환한다")
            void it_return_token_pair() {
                //given
                given(userRepository.findByEmail(email)).willReturn(Optional.of(savedUser));
                given(passwordEncoder.matches(any(String.class), any(String.class))).willReturn(true);
                given(tokenProvider.issueKeyPair(any(Long.class), any(String.class), any(Role.class)))
                        .willReturn(keyPair);
                //when
                KeyPair keyPair = userService.login(requestDto);
                //then
                Assertions.assertNotNull(keyPair.accessToken());
                Assertions.assertNotNull(keyPair.refreshToken());
                Assertions.assertNotNull(keyPair.accessExpiresInSeconds());
            }
        }

        @Nested
        @DisplayName("잘못된 이메일이 들어오면")
        class Context_with_wrong_email {

            @Test
            @DisplayName("로그인 오류가 발생한다")
            void it_throws_exist_email() {
                //given
                given(userRepository.findByEmail(email)).willReturn(Optional.empty());

                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> userService.login(requestDto)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.LOGIN_NOT_MACH.getMessage());
            }
        }

        @Nested
        @DisplayName("잘못된 비밀번호가 들어오면")
        class Context_with_wrong_password {

            @Test
            @DisplayName("로그인 오류가 발생한다")
            void it_throws_exist_email() {
                //given
                given(userRepository.findByEmail(email)).willReturn(Optional.of(savedUser));
                given(passwordEncoder.matches(any(String.class), any(String.class))).willReturn(false);

                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> userService.login(requestDto)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.LOGIN_NOT_MACH.getMessage());
            }
        }

    }

    @Nested
    @DisplayName("refresh 메서드는")
    public class Refresh {
        String refreshTokenString;
        RefreshToken refreshToken;
        RefreshToken refreshTokenEnd;
        User savedUser;
        KeyPair keyPair;
        RefreshRequestDto requestDto;
        @BeforeEach
        void setUp() {
            refreshTokenString = "refresh-token";
            savedUser = new User(1L, "newgamer@test.com","encodedpassword123","새싹게이머");
            refreshTokenEnd = new RefreshToken(refreshTokenString, LocalDateTime.of(2026,5,15,10,5),savedUser);
            refreshToken = new RefreshToken(refreshTokenString, LocalDateTime.of(2026,6,15,10,5),savedUser);
            keyPair = new KeyPair(
                    "access-token",
                    "refresh-token",
                    900L
            );
            requestDto = new RefreshRequestDto(refreshTokenString);
        }

        @Nested
        @DisplayName("유효한 입력이 주어지면")
        class Context_with_valid_request {
            @Test
            @DisplayName("새로운 accessToken을 담은 keyPair를 반환한다")
            void it_return_token_pair() {
                //given
                given(refreshRepository.findByRefreshToken(refreshTokenString)).willReturn(Optional.of(refreshToken));
                given(tokenProvider.issueKeyPair(any(Long.class), any(String.class), any(Role.class)))
                        .willReturn(keyPair);
                given(tokenProvider.parseExpiration(keyPair.refreshToken()))
                        .willReturn(new Date(20260729));
                //when
                KeyPair keyPair = userService.refresh(new RefreshRequestDto(refreshTokenString));
                //then
                Assertions.assertNotNull(keyPair.accessToken());
                Assertions.assertNotNull(keyPair.refreshToken());
                Assertions.assertNotNull(keyPair.accessExpiresInSeconds());
            }
        }
        @Nested
        @DisplayName("잘못된 refresh토큰이 주어지면")
        class Context_with_invalid_request {
            @Test
            @DisplayName("(잘못된 형식) 유효하지 않은 토큰 에러가 발생한다")
            void it_throws_invalid_refresh_token() {
                //given
                given(refreshRepository.findByRefreshToken(refreshTokenString)).willReturn(Optional.empty());
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> userService.refresh(requestDto)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
            }

            @Test
            @DisplayName("(유효시간 만료) 유효하지 않은 토큰 에러가 발생한다")
            void it_throws_invalid_refresh_token2() {
                //given
                given(refreshRepository.findByRefreshToken(refreshTokenString)).willReturn(Optional.of(refreshTokenEnd));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> userService.refresh(requestDto)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
            }
        }



    }
}