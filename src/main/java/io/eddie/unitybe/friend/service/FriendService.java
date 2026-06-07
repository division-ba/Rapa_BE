package io.eddie.unitybe.friend.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.friend.domain.Friend;
import io.eddie.unitybe.friend.dto.FriendRequestDto;
import io.eddie.unitybe.friend.dto.FriendRequestResponseDto;
import io.eddie.unitybe.friend.repository.FriendRepository;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.AuthUser;
import io.eddie.unitybe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
