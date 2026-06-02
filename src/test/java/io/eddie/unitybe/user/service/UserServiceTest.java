package io.eddie.unitybe.user.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.SignUpRequestDto;
import io.eddie.unitybe.user.dto.SignUpResponseDto;
import io.eddie.unitybe.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    PasswordEncoder passwordEncoder;



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
}