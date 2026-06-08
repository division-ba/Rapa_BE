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
import java.util.List;
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
    AuthUser authUser2;
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
        authUser2 = new AuthUser(toUserId, email2, encodedPassword, "USER");
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
    @Nested
    @DisplayName("acceptRequest 메서드는")
    public class acceptRequest {
        Long requestId = 1L;
        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(friend, "id", 1L);
            ReflectionTestUtils.setField(friend, "createdAt", LocalDateTime.now());
        }
        @Nested
        @DisplayName("로그인이 된 상태에서 유효한 입력이 주어지면")
        class Context_with_valid_request{
            @Test
            @DisplayName("변경된 요청상태를 저장하고 반환한다")
            void it_return_request_information(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                //when
                FriendRequestResponseDto response = friendService.acceptRequest(authUser2, requestId);
                //then
                Assertions.assertNotNull(response);
                assertThat(response.friendRequestId()).isEqualTo(requestId);
                assertThat(response.fromUserId()).isEqualTo(fromUserId);
                assertThat(response.toUserId()).isEqualTo(toUserId);
                assertThat(response.status()).isEqualTo(FriendStatus.ACCEPTED);
                assertThat(response.createdAt()).isNotNull();
                assertThat(response.nickname()).isEqualTo(nickname1);
            }
        }
        @Nested
        @DisplayName("요청 아이디가 유효하지 않다면")
        class Context_with_invalid_requestId{
            @Test
            @DisplayName("요청을 찾을 수 없다는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.empty());
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.acceptRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.FRIEND_NOT_FOUND.getMessage());
            }
        }
        @Nested
        @DisplayName("요청상태가 대기상태가 아니라면")
        class Context_with_not_pending_request{
            @BeforeEach
            void setUp() {
                friend.setStatus(FriendStatus.ACCEPTED);
            }
            @Test
            @DisplayName("대기상태가 아니라는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.acceptRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.NOT_STATUS_PENDING.getMessage());
            }
        }
        @Nested
        @DisplayName("본인에게 온 요청이 아니라면")
        class Context_with_not_request_recipient{
            @BeforeEach
            void setUp() {
                authUser2 = new AuthUser(3L, email2, encodedPassword, "USER");
            }
            @Test
            @DisplayName("수신자가 아니라는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.acceptRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.ACCEPTED_NOT_REQUEST_RECIPIENT.getMessage());
            }
        }
    }
    @Nested
    @DisplayName("declineRequest 메서드는")
    public class DeclineRequest {
        Long requestId = 1L;
        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(friend, "id", 1L);
            ReflectionTestUtils.setField(friend, "createdAt", LocalDateTime.now());
        }
        @Nested
        @DisplayName("로그인이 된 상태에서 유효한 입력이 주어지면")
        class Context_with_valid_request{
            @Test
            @DisplayName("변경된 요청상태를 저장한다")
            void it_return_request_information(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                //when
                friendService.declineRequest(authUser2, requestId);
                //then
                assertThat(friend.getStatus()).isEqualTo(FriendStatus.DECLINED);
            }
        }
        @Nested
        @DisplayName("요청 아이디가 유효하지 않다면")
        class Context_with_invalid_requestId{
            @Test
            @DisplayName("요청을 찾을 수 없다는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.empty());
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.declineRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.FRIEND_NOT_FOUND.getMessage());
            }
        }
        @Nested
        @DisplayName("요청상태가 대기상태가 아니라면")
        class Context_with_not_pending_request{
            @BeforeEach
            void setUp() {
                friend.setStatus(FriendStatus.ACCEPTED);
            }
            @Test
            @DisplayName("대기상태가 아니라는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.declineRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.NOT_STATUS_PENDING.getMessage());
            }
        }
        @Nested
        @DisplayName("본인에게 온 요청이 아니라면")
        class Context_with_not_request_recipient{
            @BeforeEach
            void setUp() {
                authUser2 = new AuthUser(3L, email2, encodedPassword, "USER");
            }
            @Test
            @DisplayName("수신자가 아니라는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.declineRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.DECLINED_NOT_REQUEST_RECIPIENT.getMessage());
            }
        }
    }
    @Nested
    @DisplayName("canceledRequest 메서드는")
    public class CanceledRequest {
        Long requestId = 1L;
        @BeforeEach
        void setUp() {
        }
        @Nested
        @DisplayName("로그인이 된 상태에서 유효한 입력이 주어지면")
        class Context_with_valid_request{
            @Test
            @DisplayName("변경된 요청상태를 저장한다")
            void it_return_request_information(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                //when
                friendService.canceledRequest(authUser, requestId);
                //then
                assertThat(friend.getStatus()).isEqualTo(FriendStatus.CANCELLED);
            }
        }
        @Nested
        @DisplayName("요청 아이디가 유효하지 않다면")
        class Context_with_invalid_requestId{
            @Test
            @DisplayName("요청을 찾을 수 없다는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.empty());
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.canceledRequest(authUser, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.FRIEND_NOT_FOUND.getMessage());
            }
        }
        @Nested
        @DisplayName("요청상태가 대기상태가 아니라면")
        class Context_with_not_pending_request{
            @BeforeEach
            void setUp() {
                friend.setStatus(FriendStatus.ACCEPTED);
            }
            @Test
            @DisplayName("대기상태가 아니라는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.canceledRequest(authUser, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.NOT_STATUS_PENDING.getMessage());
            }
        }
        @Nested
        @DisplayName("본인이 보낸 요청이 아니라면")
        class Context_with_not_request_recipient{
            @Test
            @DisplayName("수신자가 아니라는 에러를 반환한다")
            void it_throws_not_status_pending(){
                //given
                given(friendRepository.findById(requestId)).willReturn(Optional.of(friend));
                // when & then
                DiversionException exception = assertThrows(
                        DiversionException.class,() -> friendService.canceledRequest(authUser2, requestId)
                );
                assertThat(exception.getMessage()).isEqualTo(ErrorCode.CANCELED_NOT_REQUEST_SENDER.getMessage());
            }
        }
    }
    @Nested
    @DisplayName("getRequestList 메서드는")
    public class GetRequestList {
        AuthUser authUser3;
        private Long fromUserId2 = 3L;
        private User fromUser2;
        String email3 = "gamer2@test.com";
        String nickname3 = "새싹게이머2";
        Friend friend2;

        @BeforeEach
        void setUp() {
            authUser3 = new AuthUser(fromUserId2, email3, encodedPassword, "USER");
            fromUser2 = new User(email3, encodedPassword, nickname3);
            friend2 = new Friend(fromUser2, toUser);
            ReflectionTestUtils.setField(fromUser2, "id", fromUserId2);
            ReflectionTestUtils.setField(friend2, "id", 2L);
            ReflectionTestUtils.setField(friend, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(friend2, "createdAt", LocalDateTime.now());

        }


        @Nested
        @DisplayName("로그인이 된 상태로 유효한 요청이라면")
        class Context_with_valid_request{
            @Test
            @DisplayName("요청된 데이터를 반환한다")
            void it_return_request_information(){
                //given
                given(friendRepository.findAllByToUserIdAndStatus(authUser2.getId(), FriendStatus.PENDING))
                        .willReturn(List.of(friend, friend2));
                //when
                List<FriendRequestResponseDto> responseList = friendService.getRequestList(authUser2);
                //then
                assertThat((responseList.size())).isEqualTo(2);
                assertThat(responseList.getFirst().friendRequestId()).isEqualTo(friend.getId());
                assertThat(responseList.getFirst().fromUserId()).isEqualTo(fromUserId);
                assertThat(responseList.getFirst().toUserId()).isEqualTo(toUserId);
                assertThat(responseList.getFirst().status()).isEqualTo(FriendStatus.PENDING);
                assertThat(responseList.getFirst().createdAt()).isNotNull();
                assertThat(responseList.getFirst().nickname()).isEqualTo(nickname1);

                assertThat(responseList.get(1).friendRequestId()).isEqualTo(friend2.getId());
                assertThat(responseList.get(1).fromUserId()).isEqualTo(fromUserId2);
                assertThat(responseList.get(1).toUserId()).isEqualTo(toUserId);
                assertThat(responseList.get(1).status()).isEqualTo(FriendStatus.PENDING);
                assertThat(responseList.get(1).createdAt()).isNotNull();
                assertThat(responseList.get(1).nickname()).isEqualTo(nickname3);
            }
        }
    }

}