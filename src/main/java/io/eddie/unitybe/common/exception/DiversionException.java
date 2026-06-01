package io.eddie.unitybe.common.exception;

import lombok.Getter;

@Getter
public class DiversionException extends RuntimeException {
    private final ErrorCode errorCode;

    public DiversionException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public DiversionException(ErrorCode errorCode, String message) {
        super(errorCode.getMessage()+" "+message);
        this.errorCode = errorCode;
    }

}
