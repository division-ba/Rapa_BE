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
    NOT_FOUND_PLAYER(HttpStatus.UNAUTHORIZED, "해당하는 id의 플레이어를 찾을 수 없습니다."),

    /*Inventory*/
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 id의 아이템이 존재하지 않습니다."),

    /*NPC*/
    NPC_NOT_FOUND(HttpStatus.NOT_FOUND, "NPC를 찾을 수 없습니다."),

    /*Common*/
    DIVISION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 에러입니다.");
    private final HttpStatus status;
    private final String message;
}
