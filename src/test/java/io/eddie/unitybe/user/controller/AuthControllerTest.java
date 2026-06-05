package io.eddie.unitybe.user.controller;

import io.eddie.unitybe.common.config.JwtAuthenticationFilter;
import io.eddie.unitybe.common.config.SecurityConfig;
import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.KeyPair;
import io.eddie.unitybe.user.dto.LogInRequestDto;
import io.eddie.unitybe.user.dto.RefreshRequestDto;
import io.eddie.unitybe.user.service.TokenProvider;
import io.eddie.unitybe.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@DisplayName("AuthController 클래스의")
class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;  //가짜 HTTP 요청

    @Autowired
    ObjectMapper om;  //DTO → JSON 변환

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenProvider tokenProvider;

    String email;
    String password;
    String accessToken;
    String refreshToken;
    Long accessExpiresInSeconds;

    LogInRequestDto logInRequestDto;
    RefreshRequestDto  refreshRequestDto;
    KeyPair keyPair;

    AuthUser authUser;

    @BeforeEach
    void setUp() {
        email = "newgamer@test.com";
        password = "mypassword123";
        accessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJnYW1lckB0ZXN0LmNvbSIsIm5pY2tuYW1lIjoi7IOI7Iu56rKM7J2066i4Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODA1NDk3MTYsImV4cCI6MTc4MDU1MDYxNn0.bysmg_0tIBg75LZjkTbnk7JX6E8Snfi5bUqZpw-C5WCjCi7JQo_pmJw-6m9I1VeWR3VtALrlOKhWzXvMZrnkqQ";
        refreshToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJnYW1lckB0ZXN0LmNvbSIsIm5pY2tuYW1lIjoi7IOI7Iu56rKM7J2066i4Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODA1NDk3MTYsImV4cCI6MTc4MTE1NDUxNn0.OSLvXW1bqKPlXW6RYwY6xnlsNOpBvV_cVysKmCzI66mXCOZ45UOpcyuVZDI39l-eMPvsQUQ_NPXEep8N8T-2Jw";
        accessExpiresInSeconds = 900L;

        logInRequestDto = new LogInRequestDto(email, password);
        refreshRequestDto = new RefreshRequestDto(refreshToken);
        keyPair = new KeyPair(accessToken, refreshToken, accessExpiresInSeconds);
        authUser = new AuthUser(1L, email, password, "USER");
    }

    @Nested
    @DisplayName("POST /login 엔드포인트는")
    class Login {
        @Nested
        @DisplayName("유효한 입력값이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 토큰페어을 반환한다")
            void it_return_200_ok_and_tokens_body() throws Exception {
                //given
                given(userService.login(logInRequestDto)).willReturn(keyPair);
                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(logInRequestDto))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("로그인되었습니다."))
                        .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                        .andExpect(jsonPath("$.data.refreshToken").value(refreshToken))
                        .andExpect(jsonPath("$.data.accessExpiresInSeconds").value(accessExpiresInSeconds))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("유효하지 않은 이메일일때")
        class Context_with_invalid_email {
            @Test
            @DisplayName("(형식 맞지 않음) 400오류와 이메일 형식 오류 메시지를 돌려준다")
            void it_throws_400_and_return_invalid_email() throws Exception {
                //given
                email = "newgamer";
                logInRequestDto = new LogInRequestDto(email, password);
                given(userService.login(logInRequestDto)).willReturn(keyPair);

                //whenthen
                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(logInRequestDto))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_EMAIL.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("비밀번호가 형식에 맞지 않으면")
        class Context_with_invalid_password {
            @Test
            @DisplayName("(6자 이하) 400오류와 비밀번호 오류 메시지를 돌려준다")
            void it_throws_400_and_return_invalid_email() throws Exception {
                //given
                password = "mypa";
                logInRequestDto = new LogInRequestDto(email, password);
                given(userService.login(logInRequestDto)).willReturn(keyPair);

                //whenthen
                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(logInRequestDto))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PASSWORD.getMessage()))
                        .andDo(print());
            }
        }
    }


    @Nested
    @DisplayName("POST /refresh 엔드포인트는")
    class Refresh {
        @Nested
        @DisplayName("유효한 입력값이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 토큰페어을 반환한다")
            void it_return_200_ok_and_tokens_body() throws Exception {
                //given
                given(userService.refresh(refreshRequestDto)).willReturn(keyPair);
                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/refresh")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(refreshRequestDto))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").isEmpty())
                        .andExpect(jsonPath("$.data.accessToken").value(accessToken))
                        .andExpect(jsonPath("$.data.refreshToken").value(refreshToken))
                        .andExpect(jsonPath("$.data.accessExpiresInSeconds").value(accessExpiresInSeconds))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("유효하지 않은 refresh 토큰이 주어지면")
        class Context_with_invalid_refresh_token {

            @Test
            @DisplayName("401 상태와 INVALID_REFRESH_TOKEN 에러을 반환한다")
            void it_throws_401_and_return_invalid_refresh_token() throws Exception {
                //given
                given(userService.refresh(refreshRequestDto))
                        .willThrow(new DiversionException(ErrorCode.INVALID_REFRESH_TOKEN));
                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/refresh")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(refreshRequestDto))
                        )
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()))
                        .andExpect(jsonPath("$.data").isEmpty())
                        .andDo(print());
            }
        }

    }

    @Nested
    @DisplayName("POST /logout 엔드포인트는")
    class Logout {
        @Nested
        @DisplayName("유효한 입력값이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 메세지을 반환한다")
            void it_return_200_ok_and_message() throws Exception {

                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/logout")
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
                        .andExpect(jsonPath("$.message").value("로그아웃되었습니다."))
                        .andExpect(jsonPath("$.data").isEmpty())
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("토큰이 없다면")
        class Context_without_token {

            @Test
            @DisplayName("401 상태와 메세지을 반환한다")
            void it_return_401_and_message() throws Exception {

                //when-then
                mockMvc.perform(
                                post("/api/v1/auth/logout")
                                        .with(csrf())
                        )
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))  //application/json;charset=UTF-8
                        .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                        .andExpect(jsonPath("$.data").isEmpty())
                        .andDo(print());
            }
        }
    }

}