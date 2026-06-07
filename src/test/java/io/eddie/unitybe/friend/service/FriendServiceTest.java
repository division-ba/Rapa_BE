package io.eddie.unitybe.friend.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.friend.domain.Friend;
import io.eddie.unitybe.friend.domain.FriendStatus;
import io.eddie.unitybe.friend.dto.FriendRequestDto;
import io.eddie.unitybe.friend.dto.FriendRequestResponseDto;
import io.eddie.unitybe.friend.repository.FriendRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @InjectMocks
    private FriendService friendService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendRepository friendRepository;

    AuthUser authUser;
    private Long fromUserId = 1L;
    private Long toUserId = 2L;
    private User fromUser;
    private User toUser;

    String email1 = "gamer@test.com";
    String email2 = "gamer1@test.com";
    String encodedPassword = "encodedpassword123";
    String nickname1 = "새싹게이머";
    String nickname2 = "새싹게이머1";

    Friend friend;

    @BeforeEach
    void setUp() {
        authUser = new AuthUser(fromUserId, email1, encodedPassword, "USER");
        fromUser = new User(email1, encodedPassword, nickname1);
        toUser = new User(email2, encodedPassword, nickname2);
        friend = new Friend(fromUser, toUser);
        ReflectionTestUtils.setField(fromUser, "id", fromUserId);
        ReflectionTestUtils.setField(toUser, "id", toUserId);
    }


    @Nested
    @DisplayName("requestFriend 메서드는")
    public class RequestFriend {

        private FriendRequestDto request;
        @BeforeEach
        void setUp() {
            request = new FriendRequestDto(toUserId);
        }

        @Nested
        @DisplayName("로그인이 된 상태에서 유효한 입력이 주어지면")
        class Context_with_valid_request {
            @Test
            @DisplayName("해당 친구요청을 저장하고 반환한다")
            void it_return_data_information() {
                //given
                given(friendRepository.existsFriendBetween(fromUserId, toUserId)).willReturn(Boolean.FALSE);
                given(userRepository.findById(authUser.getId())).willReturn(Optional.of(fromUser));
                given(userRepository.findById(toUserId)).willReturn(Optional.of(toUser));

                //repository에 저장해서 id, createdAt 자동 생성
                given(friendRepository.save(any(Friend.class)))
                        .willAnswer(invocation -> {
                            Friend f = invocation.getArgument(0);
                            ReflectionTestUtils.setField(f, "id", 1L);
                            ReflectionTestUtils.setField(f, "createdAt", LocalDateTime.now());
                            return f;
                        });

                //when
                FriendRequestResponseDto response = friendService.requestFriend(authUser, request);

                //then
                Assertions.assertNotNull(response);
                System.out.println("response.friendRequestId() = " + response.friendRequestId());
                assertThat(response.friendRequestId()).isNotNull();
                assertThat(response.fromUserId()).isEqualTo(fromUserId);
                assertThat(response.toUserId()).isEqualTo(toUserId);
                assertThat(response.status()).isEqualTo(FriendStatus.PENDING);
                assertThat(response.createdAt()).isNotNull();
                assertThat(response.nickname()).isEqualTo(nickname2);
            }
        }
        @Nested
        @DisplayName("로그인이 된 상태에서 자기자신에게 보낸다면")
        class Context_with_self_request {
            @BeforeEach
            void setUp() {
                request = new FriendRequestDto(fromUserId);
            }
            @Test
            @DisplayName("자기자신 요청이라는 에러가 발생한다")
            void it_throws_self_friend_request() {
                //given

                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.requestFriend(authUser, request)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.SELF_FRIEND_REQUEST.getMessage());
            }
        }
        @Nested
        @DisplayName("이미 친구요청을 보냈었다면")
        class Context_with_existing_request {
            @Test
            @DisplayName("이미 존재하는 요청이라고 에러가 발생한다")
            void it_throws_exist_friend_request() {
                //given
                given(friendRepository.existsFriendBetween(authUser.getId(), request.toUserId())).willReturn(Boolean.TRUE);
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.requestFriend(authUser, request)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.EXIST_FRIEND_REQUEST.getMessage());
            }
        }

    }


}