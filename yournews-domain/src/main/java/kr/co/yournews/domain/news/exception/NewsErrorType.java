package kr.co.yournews.domain.news.exception;

import kr.co.yournews.common.response.StatusCode;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.RequiredArgsConstructor;

/**
 * News ErrorCode: Nxxx
 */
@RequiredArgsConstructor
public enum NewsErrorType implements BaseErrorType {

    NOT_FOUND(StatusCode.NOT_FOUND, "N001", "존재하지 않는 소식입니다."),
    INVALID_KEYWORD(StatusCode.BAD_REQUEST, "N002", "지원하지 않는 키워드입니다.")
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
