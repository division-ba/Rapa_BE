package io.eddie.unitybe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    /*User*/
    EXIST_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다.."),
    /*Inventory*/
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 id의 아이템이 존재하지 않습니다."),

    /*Common*/
    DIVISION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 에러입니다.");
    private final HttpStatus status;
    private final String message;
}
