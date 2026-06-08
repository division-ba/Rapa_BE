package io.eddie.unitybe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /*User*/
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일 형식으로 입력해주세요."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호는 8~64자여야 합니다."),
    EXIST_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 id의 유저를 찾을 수 없습니다."),
    USER_NOT_FOUND_BY_EMAIL(HttpStatus.NOT_FOUND, "해당하는 이메일의 유저를 찾을 수 없습니다."),
    LOGIN_NOT_MACH(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    /*Player*/
    PLAYER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "해당하는 id의 플레이어를 찾을 수 없습니다."),

    /*Friend*/
    SELF_FRIEND_REQUEST(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다."),
    EXIST_FRIEND_REQUEST(HttpStatus.CONFLICT, "이미 보낸 친구 요청이 있습니다."),
    EXIST_FRIEND(HttpStatus.CONFLICT, "이미 친구 관계입니다. "),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 id의 친구요청을 찾을 수 없습니다."),
    ACCEPTED_NOT_REQUEST_RECIPIENT(HttpStatus.BAD_REQUEST, "본인에게 온 요청만 수락할 수 있습니다."),
    DECLINED_NOT_REQUEST_RECIPIENT(HttpStatus.BAD_REQUEST, "본인에게 온 요청만 거절할 수 있습니다."),
    NOT_STATUS_PENDING(HttpStatus.CONFLICT, "요청이 대기상태가 아닙니다."),
    /*Inventory*/
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 id의 아이템이 존재하지 않습니다."),
    INVALID_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "아이템 수량은 1 이상이어야 합니다."),

    /*NPC*/
    NPC_NOT_FOUND(HttpStatus.NOT_FOUND, "NPC를 찾을 수 없습니다."),

    /*Common*/
    DIVISION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 에러입니다.");
    private final HttpStatus status;
    private final String message;
}
