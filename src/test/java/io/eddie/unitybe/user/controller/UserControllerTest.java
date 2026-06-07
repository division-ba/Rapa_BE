package io.eddie.unitybe.user.controller;

import io.eddie.unitybe.common.config.JwtAuthenticationFilter;
import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.dto.SignUpRequestDto;
import io.eddie.unitybe.user.dto.SignUpResponseDto;
import io.eddie.unitybe.user.dto.UserAccountResponseDto;
import io.eddie.unitybe.user.service.UserService;
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
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
//@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})  // security 설정 가져오기
@AutoConfigureMockMvc(addFilters = false)  // 필터 끄기
@DisplayName("UserController 클래스의")
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;  //가짜 HTTP 요청

    @Autowired
    ObjectMapper om;  //DTO → JSON 변환

    @MockitoBean
    private UserService userService;

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

    SignUpRequestDto requestDto;
    SignUpResponseDto  responseDto;

    AuthUser authUser;
    User user;
    UserAccountResponseDto profileResponseDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        email = "newgamer@test.com";
        password = "mypassword123";
        nickname = "새싹게이머";

    }

    @Nested
    @DisplayName("POST /register 엔드포인트는")
    class Signup {
        @BeforeEach
        void setUp() {
            requestDto = new SignUpRequestDto(email, password, nickname);
            responseDto = new SignUpResponseDto(new User (userId, email, password, nickname));
        }

        @Nested
        @DisplayName("유효한 입력값이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 회원 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(userService.signup(requestDto)).willReturn(responseDto);
                //when-then
                mockMvc.perform(
                        post("/api/v1/users/register")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(nickname))
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
                requestDto = new SignUpRequestDto(email, password, nickname);
                given(userService.signup(requestDto)).willReturn(responseDto);

                //whenthen
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(requestDto))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("이메일 형식으로 입력해주세요."))
                        .andDo(print());
            }

            @Test
            @DisplayName("(중복된 이메일) 409오류와 중복이메일 오류 메시지를 돌려준다")
            void it_throws_400_and_return_exist_email() throws Exception {
                //given
                requestDto = new SignUpRequestDto(email, password, nickname);
                given(userService.signup(requestDto)).willThrow(new DiversionException(ErrorCode.EXIST_EMAIL));

                //whenthen
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(requestDto))
                        )
                        .andExpect(status().isConflict())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.EXIST_EMAIL.getMessage()))
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
                requestDto = new SignUpRequestDto(email, password, nickname);
                given(userService.signup(requestDto)).willReturn(responseDto);

                //when-then
                mockMvc.perform(
                                post("/api/v1/users/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(requestDto))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("비밀번호는 8~64자여야 합니다."))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("닉네임이 형식에 맞지 않으면")
        class Context_with_invalid_nickname {
            @Test
            @DisplayName("(2자 이하) 400오류와 닉네임 오류 메시지를 돌려준다")
            void it_throws_400_and_return_invalid_nickname() throws Exception {
                //given
                nickname = "r";
                requestDto = new SignUpRequestDto(email, password, nickname);
                given(userService.signup(requestDto)).willReturn(responseDto);

                //whenthen
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/register")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(requestDto))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("닉네임은 2~20자여야 합니다."))
                        .andDo(print());
            }
        }

    }

    @Nested
    @DisplayName("GET /me 엔드포인트는")
    class getMyProfile {
        @BeforeEach
        void setUp() {
            authUser = new AuthUser(userId, email, password, "USER");
            user = new User(userId, email, password, nickname);
            profileResponseDto = UserAccountResponseDto.from(user);
        }
        @Nested
        @DisplayName("유효한 토큰이 주어지면")
        class Context_with_valid_request {

            @Test
            @DisplayName("200 상태와 내 계정 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(userService.getMyProfile(any())).willReturn(profileResponseDto);
                //when-then
                mockMvc.perform(
                                get("/api/v1/users/me")
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
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.email").value(email))
                        .andExpect(jsonPath("$.data.nickname").value(nickname))
                        .andDo(print());
            }
        }
    }


}