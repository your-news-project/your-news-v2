package kr.co.yournews.common.exception;

import kr.co.yournews.common.response.error.type.BaseErrorType;
import kr.co.yournews.common.response.exception.StatusCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthErrorType implements BaseErrorType {
    ACCESS_TOKEN_EXPIRED(StatusCode.UNAUTHORIZED, "A001", "Access Token이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(StatusCode.UNAUTHORIZED, "A002", "Access Token이 잘못되었습니다."),
    INVALID_TOKEN_SIGNATURE(StatusCode.UNAUTHORIZED, "A003", "Access Token의 서명이 잘못되었습니다."),
    UNKNOWN_TOKEN_ERROR(StatusCode.UNAUTHORIZED, "A004", "알 수 없는 토큰 에러입니다."),
    REFRESH_TOKEN_NOT_FOUND(StatusCode.NOT_FOUND, "A005", "Refresh Token이 존재하지 않습니다."),
    BLACKLIST_ACCESS_TOKEN(StatusCode.UNAUTHORIZED, "A006", "접근 불가한 AccessToken입니다."),
    ;

    private final StatusCode status;
    private final String code;
    private final String message;

    @Override
    public StatusCode getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
