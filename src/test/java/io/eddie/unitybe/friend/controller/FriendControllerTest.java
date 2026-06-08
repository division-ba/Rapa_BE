package io.eddie.unitybe.friend.controller;

import io.eddie.unitybe.common.config.JwtAuthenticationFilter;
import io.eddie.unitybe.common.config.entrypoint.JwtAccessDeniedHandler;
import io.eddie.unitybe.common.config.entrypoint.JwtAuthenticationEntryPoint;
import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.friend.domain.FriendStatus;
import io.eddie.unitybe.friend.dto.FriendRequestDto;
import io.eddie.unitybe.friend.dto.FriendRequestResponseDto;
import io.eddie.unitybe.friend.service.FriendService;
import io.eddie.unitybe.user.dto.AuthUser;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FriendController 클래스의")
class FriendControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    String email = "newgamer@test.com";
    String email2 = "newgamer1@test.com";
    String password = "mypassword123";

    Long friendId = 1L;
    Long fromUserId = 1L;
    Long toUserId = 2L;
    LocalDateTime createdAt = LocalDateTime.now();
    String nickname1 = "새싹게이머";
    String nickname2 = "새싹게이머1";
    AuthUser authUser = new AuthUser(fromUserId, email, password, "USER");
    AuthUser authUser2 = new AuthUser(toUserId, email2, password, "USER");

    @Nested
    @DisplayName("POST /requests 엔드포인트는")
    class RequestFriend {
        FriendRequestDto request;
        FriendRequestResponseDto response;
        FriendStatus status;

        @BeforeEach
        void setUp() {
            status = FriendStatus.PENDING;
            response = new FriendRequestResponseDto(
                    friendId, fromUserId, toUserId, status, createdAt,nickname2
            );
        }
        @Nested
        @DisplayName("유효한 토큰과 입력값이 주어지면")
        class Context_with_valid_request {
            @BeforeEach
            void setUp() {
                request = new FriendRequestDto(toUserId);
            }
            @Test
            @DisplayName("200 상태와 친구 요청 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(friendService.requestFriend(any(), eq(request))).willReturn(response);
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(request))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.friendRequestId").value(friendId))
                        .andExpect(jsonPath("$.data.fromUserId").value(fromUserId))
                        .andExpect(jsonPath("$.data.toUserId").value(toUserId))
                        .andExpect(jsonPath("$.data.status").value(status.name()))
                        .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                        .andExpect(jsonPath("$.data.nickname").value(nickname2))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("request안의 id가 없으면")
        class Context_with_invalid_toUserId {
            @BeforeEach
            void setUp() {
                request = new FriendRequestDto(null);
            }
            @Test
            @DisplayName("400오류와 id값이 필수라는 오류 메시지를 돌려준다")
            void it_throws_400_and_return_id_is_needed() throws Exception {
                //given
                given(friendService.requestFriend(any(), eq(request))).willReturn(response);
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(request))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("요청 상대의 아이디는 필수갑입니다"))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("유효하지 않은 요청일때")
        class Context_with_invalid_request {
            @BeforeEach
            void setUp() {
                request = new FriendRequestDto(toUserId);
            }
            @Test
            @DisplayName("(자기자신에게 요청) 400오류와 자기자신 요청 오류 메시지를 돌려준다")
            void it_throws_400_and_return_self_request() throws Exception {
                //given
                given(friendService.requestFriend(any(), eq(request))).willThrow(new DiversionException(ErrorCode.SELF_FRIEND_REQUEST));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(request))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.SELF_FRIEND_REQUEST.getMessage()))
                        .andDo(print());
            }
            @Test
            @DisplayName("(이미 있는 요청) 400오류와 이미 요청이 있다는 오류 메시지를 돌려준다")
            void it_throws_400_and_return_exist_request() throws Exception {
                //given
                given(friendService.requestFriend(any(), eq(request))).willThrow(new DiversionException(ErrorCode.EXIST_FRIEND_REQUEST));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(om.writeValueAsString(request))
                        )
                        .andExpect(status().isConflict())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.EXIST_FRIEND_REQUEST.getMessage()))
                        .andDo(print());
            }
        }
    }

    @Nested
    @DisplayName("POST /requests/{requestId}/accept 엔드포인트는")
    class AcceptRequest {
        Long requestId = 1L;
        FriendStatus status;
        FriendRequestResponseDto response;
        @BeforeEach
        void setUp() {
            status = FriendStatus.ACCEPTED;
            response = new FriendRequestResponseDto(
                    friendId, fromUserId, toUserId, status, createdAt,nickname1
            );
        }
        @Nested
        @DisplayName("유효한 토큰과 입력값이 주어지면")
        class Context_with_valid_request {
            @Test
            @DisplayName("200 상태와 변경된 친구 요청 정보를 반환한다")
            void it_return_200_ok_and_response_body() throws Exception {
                //given
                given(friendService.acceptRequest(any(), eq(requestId))).willReturn(response);
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/accept")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data.friendRequestId").value(friendId))
                        .andExpect(jsonPath("$.data.fromUserId").value(fromUserId))
                        .andExpect(jsonPath("$.data.toUserId").value(toUserId))
                        .andExpect(jsonPath("$.data.status").value(status.name()))
                        .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                        .andExpect(jsonPath("$.data.nickname").value(nickname1))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("요청 아이디가 유효하지 않다면")
        class Context_with_invalid_requestId {
            @Test
            @DisplayName("404오류와 요청을 찾을 수 없다는 오류 메시지를 돌려준다")
            void it_throws_404_and_return_friend_not_found() throws Exception {
                //given
                given(friendService.acceptRequest(any(), eq(requestId)))
                        .willThrow(new DiversionException(ErrorCode.FRIEND_NOT_FOUND));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/accept")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_NOT_FOUND.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("요청상태가 대기상태가 아니라면")
        class Context_with_not_pending_request {
            @Test
            @DisplayName("409오류와 대기상태가 아니라는 오류 메시지를 돌려준다")
            void it_throws_409_and_return_status_not_pending() throws Exception {
                //given
                given(friendService.acceptRequest(any(), eq(requestId)))
                        .willThrow(new DiversionException(ErrorCode.NOT_STATUS_PENDING));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/accept")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isConflict())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.NOT_STATUS_PENDING.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("본인에게 온 요청이 아니라면")
        class Context_with_not_request_recipient {
            @Test
            @DisplayName("400오류와 수신자가 아니라는 오류 메시지를 돌려준다")
            void it_throws_400_and_return_not_request_recipient() throws Exception {
                //given
                given(friendService.acceptRequest(any(), eq(requestId)))
                        .willThrow(new DiversionException(ErrorCode.ACCEPTED_NOT_REQUEST_RECIPIENT));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/accept")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.ACCEPTED_NOT_REQUEST_RECIPIENT.getMessage()))
                        .andDo(print());
            }
        }
    }
    @Nested
    @DisplayName("POST /requests/{requestId}/decline 엔드포인트는")
    class DeclineRequest {
        Long requestId = 1L;
        FriendStatus status;
        @BeforeEach
        void setUp() {
            status = FriendStatus.DECLINED;
        }
        @Nested
        @DisplayName("유효한 토큰과 입력값이 주어지면")
        class Context_with_valid_request {
            @Test
            @DisplayName("200 상태와 성공메시지를 반환한다")
            void it_return_200_ok_and_response_success_message() throws Exception {
                //given
                doNothing().when(friendService)
                        .declineRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/decline")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value("친구 요청을 거절했습니다."))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("요청 아이디가 유효하지 않다면")
        class Context_with_invalid_requestId {
            @Test
            @DisplayName("404오류와 요청을 찾을 수 없다는 오류 메시지를 돌려준다")
            void it_throws_404_and_return_friend_not_found() throws Exception {
                //given
                doThrow(new DiversionException(ErrorCode.FRIEND_NOT_FOUND))
                        .when(friendService)
                        .declineRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/decline")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_NOT_FOUND.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("요청상태가 대기상태가 아니라면")
        class Context_with_not_pending_request {
            @Test
            @DisplayName("409오류와 대기상태가 아니라는 오류 메시지를 돌려준다")
            void it_throws_409_and_return_status_not_pending() throws Exception {
                //given
                doThrow(new DiversionException(ErrorCode.NOT_STATUS_PENDING))
                        .when(friendService)
                        .declineRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/decline")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isConflict())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.NOT_STATUS_PENDING.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("본인에게 온 요청이 아니라면")
        class Context_with_not_request_recipient {
            @Test
            @DisplayName("400오류와 수신자가 아니라는 오류 메시지를 돌려준다")
            void it_throws_400_and_return_not_request_recipient() throws Exception {
                //given
                doThrow(new DiversionException(ErrorCode.DECLINED_NOT_REQUEST_RECIPIENT))
                        .when(friendService)
                        .declineRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                post("/api/v1/users/me/friends/requests/1/decline")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.DECLINED_NOT_REQUEST_RECIPIENT.getMessage()))
                        .andDo(print());
            }
        }
    }
    @Nested
    @DisplayName("DELETE /requests/{requestId} 엔드포인트는")
    class CanceledRequest {
        Long requestId = 1L;
        FriendStatus status;
        @BeforeEach
        void setUp() {
            status = FriendStatus.CANCELLED;
        }
        @Nested
        @DisplayName("유효한 토큰과 입력값이 주어지면")
        class Context_with_valid_request {
            @Test
            @DisplayName("200 상태와 성공메시지를 반환한다")
            void it_return_200_ok_and_response_success_message() throws Exception {
                //given
                doNothing().when(friendService)
                        .canceledRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                delete("/api/v1/users/me/friends/requests/1")
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
                        .andExpect(jsonPath("$.message").value("친구 요청을 취소했습니다."))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("요청 아이디가 유효하지 않다면")
        class Context_with_invalid_requestId {
            @Test
            @DisplayName("404오류와 요청을 찾을 수 없다는 오류 메시지를 돌려준다")
            void it_throws_404_and_return_friend_not_found() throws Exception {
                //given
                doThrow(new DiversionException(ErrorCode.FRIEND_NOT_FOUND))
                        .when(friendService)
                        .canceledRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                delete("/api/v1/users/me/friends/requests/1")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isNotFound())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.FRIEND_NOT_FOUND.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("요청상태가 대기상태가 아니라면")
        class Context_with_not_pending_request {
            @Test
            @DisplayName("409오류와 대기상태가 아니라는 오류 메시지를 돌려준다")
            void it_throws_409_and_return_status_not_pending() throws Exception {
                //given
                doThrow(new DiversionException(ErrorCode.NOT_STATUS_PENDING))
                        .when(friendService)
                        .canceledRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                delete("/api/v1/users/me/friends/requests/1")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isConflict())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.NOT_STATUS_PENDING.getMessage()))
                        .andDo(print());
            }
        }

        @Nested
        @DisplayName("본인에게 온 요청이 아니라면")
        class Context_with_not_request_recipient {
            @Test
            @DisplayName("400오류와 발신자가 아니라는 오류 메시지를 돌려준다")
            void it_throws_400_and_return_not_request_sender() throws Exception {
                //given
                doThrow(new DiversionException(ErrorCode.CANCELED_NOT_REQUEST_SENDER))
                        .when(friendService)
                        .canceledRequest(any(), eq(requestId));
                //when-then
                mockMvc.perform(
                                delete("/api/v1/users/me/friends/requests/1")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser,
                                                        null,
                                                        authUser.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message").value(ErrorCode.CANCELED_NOT_REQUEST_SENDER.getMessage()))
                        .andDo(print());
            }
        }
    }
    @Nested
    @DisplayName("GET /requests 엔드포인트는")
    class GetRequestList {
        @Nested
        @DisplayName("유효한 토큰이 주어지면")
        class Context_with_valid_request {
            List<FriendRequestResponseDto> response;
            FriendStatus status =  FriendStatus.PENDING;
            Long friendId2 = 2L;
            Long fromUserId2 = 3L;
            String nickname3 = "새싹게이머2";
            @BeforeEach
            void setup() {
                response = List.of(
                        new FriendRequestResponseDto(
                                friendId, fromUserId, toUserId, status, createdAt,nickname1),
                        new FriendRequestResponseDto(
                                friendId2, fromUserId2, toUserId, status, createdAt,nickname3)
                );
            }
            @Test
            @DisplayName("200 상태와 해당 데이터리스트를 반환한다")
            void it_return_200_ok_and_response_data_list() throws Exception {
                //given
                given(friendService.getRequestList(any())).willReturn(response);
                //when-then
                mockMvc.perform(
                                get("/api/v1/users/me/friends/requests")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data[0].friendRequestId").value(friendId))
                        .andExpect(jsonPath("$.data[0].fromUserId").value(fromUserId))
                        .andExpect(jsonPath("$.data[0].toUserId").value(toUserId))
                        .andExpect(jsonPath("$.data[0].status").value(status.name()))
                        .andExpect(jsonPath("$.data[0].createdAt").isNotEmpty())
                        .andExpect(jsonPath("$.data[0].nickname").value(nickname1))

                        .andExpect(jsonPath("$.data[1].friendRequestId").value(friendId2))
                        .andExpect(jsonPath("$.data[1].fromUserId").value(fromUserId2))
                        .andExpect(jsonPath("$.data[1].toUserId").value(toUserId))
                        .andExpect(jsonPath("$.data[1].status").value(status.name()))
                        .andExpect(jsonPath("$.data[1].createdAt").isNotEmpty())
                        .andExpect(jsonPath("$.data[1].nickname").value(nickname3))
                        .andDo(print());
            }
        }
    }
    @Nested
    @DisplayName("GET 엔드포인트는")
    class GetFriendList {
        @Nested
        @DisplayName("유효한 토큰이 주어지면")
        class Context_with_valid_request {
            List<FriendRequestResponseDto> response;
            FriendStatus status =  FriendStatus.ACCEPTED;
            Long friendId2 = 2L;
            Long fromUserId2 = 3L;
            String nickname3 = "새싹게이머2";
            @BeforeEach
            void setup() {
                response = List.of(
                        new FriendRequestResponseDto(
                                friendId, fromUserId, toUserId, status, createdAt,nickname1),
                        new FriendRequestResponseDto(
                                friendId2, fromUserId2, toUserId, status, createdAt,nickname3)
                );
            }
            @Test
            @DisplayName("200 상태와 해당 데이터리스트를 반환한다")
            void it_return_200_ok_and_response_data_list() throws Exception {
                //given
                given(friendService.getFriendList(any())).willReturn(response);
                //when-then
                mockMvc.perform(
                                get("/api/v1/users/me/friends")
                                        .with(csrf())
                                        .with(authentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        authUser2,
                                                        null,
                                                        authUser2.getAuthorities()
                                                )
                                        ))
                        )
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.data[0].friendRequestId").value(friendId))
                        .andExpect(jsonPath("$.data[0].fromUserId").value(fromUserId))
                        .andExpect(jsonPath("$.data[0].toUserId").value(toUserId))
                        .andExpect(jsonPath("$.data[0].status").value(status.name()))
                        .andExpect(jsonPath("$.data[0].createdAt").isNotEmpty())
                        .andExpect(jsonPath("$.data[0].nickname").value(nickname1))

                        .andExpect(jsonPath("$.data[1].friendRequestId").value(friendId2))
                        .andExpect(jsonPath("$.data[1].fromUserId").value(fromUserId2))
                        .andExpect(jsonPath("$.data[1].toUserId").value(toUserId))
                        .andExpect(jsonPath("$.data[1].status").value(status.name()))
                        .andExpect(jsonPath("$.data[1].createdAt").isNotEmpty())
                        .andExpect(jsonPath("$.data[1].nickname").value(nickname3))
                        .andDo(print());
            }
        }
    }
}