package io.eddie.unitybe.player.controller;

import io.eddie.unitybe.common.config.JwtAuthenticationFilter;
import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import io.eddie.unitybe.player.domain.Player;
import io.eddie.unitybe.player.dto.UserDataResponseDto;
import io.eddie.unitybe.player.dto.UserProfileResponseDto;
import io.eddie.unitybe.player.dto.UserWalletResponseDto;
import io.eddie.unitybe.player.service.PlayerService;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlayerController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController 클래스의")
class PlayerControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    Long userId;
    String email;
    String password;
    String nickname;
    AuthUser authUser;
    User user;
    Player player;

    @BeforeEach
    void setUp() {
        userId = 1L;
        email = "newgamer@test.com";
        password = "mypassword123";
        nickname = "새싹게이머";
        authUser = new AuthUser(userId, email, password, "USER");
        user = new User(userId, email, password, nickname);
        player = new Player(user);

    }

    @Nested
    @DisplayName("GET /data 엔드포인트는")
    class getMyData {

        UserDataResponseDto dataResponseDto;

        @BeforeEach
        void setUp() {
            dataResponseDto = new UserDataResponseDto(
                    UserAccountResponseDto.from(user),
                    new UserProfileResponseDto(player.getLevel(), player.getExp(), player.getTotalPlaySeconds()),
                    new UserWalletResponseDto(player.getGold(), player.getGem()),
                    null, null
            );
        }

        @Nested
        @DisplayName("유효한 토큰이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 내 계정 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(playerService.getUserData(any())).willReturn(dataResponseDto);
                //when-then
                mockMvc.perform(
                                get("/api/v1/users/me/data")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.account.userId").value(userId))
                        .andExpect(jsonPath("$.data.account.email").value(email))
                        .andExpect(jsonPath("$.data.account.nickname").value(nickname))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("GET /profile 엔드포인트는")
    class getMyProfile {
        UserProfileResponseDto responseDto;
        Integer level = 1;
        Long exp = 0L;
        Long totalPlaySeconds = 0L;
        @BeforeEach
        void setUp() {
            responseDto = new UserProfileResponseDto(
                    player.getLevel(), player.getExp(), player.getTotalPlaySeconds());
        }

        @Nested
        @DisplayName("유효한 토큰이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 내 계정 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(playerService.getUserProfile(any())).willReturn(responseDto);
                //when-then
                mockMvc.perform(
                                get("/api/v1/users/me/profile")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.level").value(level))
                        .andExpect(jsonPath("$.data.exp").value(exp))
                        .andExpect(jsonPath("$.data.totalPlaySeconds").value(totalPlaySeconds))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("GET /wallet 엔드포인트는")
    class getMyWallet {
        UserWalletResponseDto responseDto;
        Long gold = 0L;
        Long gem = 0L;
        @BeforeEach
        void setUp() {
            responseDto = new UserWalletResponseDto(gold, gem);
        }

        @Nested
        @DisplayName("유효한 토큰이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 내 지갑 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(playerService.getUserWallet(any())).willReturn(responseDto);
                //when-then
                mockMvc.perform(
                                get("/api/v1/users/me/wallet")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.gold").value(gold))
                        .andExpect(jsonPath("$.data.gem").value(gem))
                        .andDo(print());
            }
        }
    }
}