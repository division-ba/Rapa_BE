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
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public FriendRequestResponseDto requestFriend(AuthUser authUser, FriendRequestDto request) {
        // 자기자신에게 보낸 경우 에러 반환
        if (Objects.equals(authUser.getId(), request.toUserId()))
            throw new DiversionException(ErrorCode.SELF_FRIEND_REQUEST);

        // 친구 요청이 있는지 확인
        if (friendRepository.existsFriendBetween(authUser.getId(), request.toUserId()))
            throw new DiversionException(ErrorCode.EXIST_FRIEND_REQUEST);

        // 유저 찾기
        User me = userRepository.findById(authUser.getId())
                .orElseThrow(()-> new DiversionException(ErrorCode.USER_NOT_FOUND));
        User toUser = userRepository.findById(request.toUserId())
                .orElseThrow(()-> new DiversionException(ErrorCode.USER_NOT_FOUND));

        // 친구 요청 저장
        Friend friend = new Friend(me, toUser);
        friendRepository.save(friend);
        return FriendRequestResponseDto.from(friend, toUser.getNickname());
    }

    //친구 요청 수락
    @Transactional
    public FriendRequestResponseDto acceptRequest(AuthUser authUser, Long requestId) {
        // 친구 요청 찾기
        Friend friend = findFriend(requestId);

        //본인에게 온 요청이 아니라면 에러 반환
        if (!friend.getToUser().getId().equals(authUser.getId()))
            throw new DiversionException(ErrorCode.ACCEPTED_NOT_REQUEST_RECIPIENT);

        // 요청 상태가 PENDING 상태가 아니라면 에러 반환
        if (!friend.getStatus().equals(FriendStatus.PENDING))
            throw new DiversionException(ErrorCode.NOT_STATUS_PENDING);


        // 상태 accepted로 변경
        friend.setStatus(FriendStatus.ACCEPTED);
        return FriendRequestResponseDto.from(friend, friend.getFromUser().getNickname());

    }

    // 친구 요청 거절
    @Transactional
    public void declineRequest(AuthUser authUser, Long requestId) {
        // 친구 요청 찾기
        Friend friend = findFriend(requestId);

        //본인에게 온 요청이 아니라면 에러 반환
        if (!friend.getToUser().getId().equals(authUser.getId()))
            throw new DiversionException(ErrorCode.DECLINED_NOT_REQUEST_RECIPIENT);

        // 요청 상태가 PENDING 상태가 아니라면 에러 반환
        if (!friend.getStatus().equals(FriendStatus.PENDING))
            throw new DiversionException(ErrorCode.NOT_STATUS_PENDING);

        // 상태 declined로 변경
        friend.setStatus(FriendStatus.DECLINED);
    }

    @Transactional
    public void canceledRequest(AuthUser authUser, Long requestId) {
        // 친구 요청 찾기
        Friend friend = findFriend(requestId);

        //본인이 보낸 요청이 아니라면 에러 반환
        if (!friend.getFromUser().getId().equals(authUser.getId()))
            throw new DiversionException(ErrorCode.CANCELED_NOT_REQUEST_SENDER);

        // 요청 상태가 PENDING 상태가 아니라면 에러 반환
        if (!friend.getStatus().equals(FriendStatus.PENDING))
            throw new DiversionException(ErrorCode.NOT_STATUS_PENDING);

        // 상태 canceled로 변경
        friend.setStatus(FriendStatus.CANCELLED);
    }

    //받은 친구 요청 목록 조회
    public List<FriendRequestResponseDto> getRequestList(AuthUser authUser) {
        List<Friend> requestList = friendRepository.findAllByToUserIdAndStatus(authUser.getId(), FriendStatus.PENDING);
        return requestList.stream()
                .map(req -> FriendRequestResponseDto.from(req, req.getFromUser().getNickname()))
                .toList();
    }

    // 친구 목록 조회
    public List<FriendRequestResponseDto> getFriendList(AuthUser authUser) {
        List<Friend> requestList = friendRepository.findFriendList(authUser.getId(), FriendStatus.ACCEPTED);
        return requestList.stream()
                .map(req ->
                {
                    if (req.getFromUser().getId().equals(authUser.getId()))
                        return FriendRequestResponseDto.from(req, req.getToUser().getNickname());
                     else
                        return FriendRequestResponseDto.from(req, req.getFromUser().getNickname());
                })
                .toList();
    }

    //친구 삭제
    @Transactional
    public void deleteFriend(AuthUser authUser, Long friendUserId) {
        // 나와 상대가 친구관계이고 상태가 accepted인 경우 찾기
        Friend friend = friendRepository.findMyFriend(authUser.getId(), friendUserId, FriendStatus.ACCEPTED)
                .orElseThrow(()-> new DiversionException(ErrorCode.NOT_FRIEND_RELATION));
        // 해당 친구 삭제
        friend.delete();
    }

    private @NonNull Friend findFriend(Long requestId) {
        return friendRepository.findById(requestId)
                .orElseThrow(() -> new DiversionException(ErrorCode.FRIEND_NOT_FOUND));
    }


}
