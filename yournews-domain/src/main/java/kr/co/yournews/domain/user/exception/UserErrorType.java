package kr.co.yournews.domain.user.exception;

import kr.co.yournews.common.response.error.type.BaseErrorType;
import kr.co.yournews.common.response.StatusCode;
import lombok.RequiredArgsConstructor;

/**
 * User ErrorCode: Uxxx
 */
@RequiredArgsConstructor
public enum UserErrorType implements BaseErrorType {

    NOT_FOUND(StatusCode.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    NOT_MATCHED_PASSWORD(StatusCode.BAD_REQUEST, "U002", "비밀번호가 일치하지 않습니다."),
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
