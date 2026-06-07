package io.eddie.unitybe.player.service;

import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.dto.UserDataResponseDto;
import io.eddie.unitybe.player.repository.PlayerRepository;
import io.eddie.unitybe.player.repository.UserPlayerRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {
    @InjectMocks
    private PlayerService playerService;

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserPlayerRepository userPlayerRepository;

    @Nested
    @DisplayName("getUserData 메서드는")
    public class GetUserData {
        AuthUser authUser;
        Long userId = 1L;
        String email = "newgamer@test.com";
        String encodedPassword = "encodedpassword123";
        String nickname = "새싹게이머";
        Player player;
        User user;

        @BeforeEach
        void setUp() {
            authUser = new AuthUser(userId, email, encodedPassword, "USER");
            user = new User(userId, email, encodedPassword, nickname);
            player = new Player(user);
        }
        @Nested
        @DisplayName("로그인이 되어있다면")
        class Context_with_valid_request {
            @Test
            @DisplayName("해당 유저의 모든 계정정보를 보여준다")
            void it_return_data_information() {
                //given
                given(playerRepository.findByUserIdWithUser(authUser.getId())).willReturn(Optional.of(player));
                //when
                UserDataResponseDto responseDto = playerService.getUserData(authUser);
                //then
                Assertions.assertNotNull(responseDto);
                assertThat(responseDto.account().email()).isEqualTo(email);
                assertThat(responseDto.account().nickname()).isEqualTo(nickname);
                assertThat(responseDto.account().role()).isNotEmpty();
                assertThat(responseDto.account().status()).isNotEmpty();
                assertThat(responseDto.account().provider()).isNotEmpty();
                assertThat(responseDto.profile().level()).isNotNull();
                assertThat(responseDto.profile().exp()).isNotNull();
                assertThat(responseDto.profile().totalPlaySeconds()).isNotNull();
                assertThat(responseDto.wallet().gold()).isNotNull();
                assertThat(responseDto.wallet().gem()).isNotNull();
            }

        }

    }

}