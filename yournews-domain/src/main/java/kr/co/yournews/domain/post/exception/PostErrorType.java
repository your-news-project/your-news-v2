package kr.co.yournews.domain.post.exception;

import kr.co.yournews.common.response.StatusCode;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.RequiredArgsConstructor;

/**
 * Post ErrorCode: Pxxx
 */
@RequiredArgsConstructor
public enum PostErrorType implements BaseErrorType {

    NOT_FOUND(StatusCode.NOT_FOUND, "P001", "존재하지 않는 게시글입니다."),
    FORBIDDEN(StatusCode.FORBIDDEN, "P002", "접근 권한이 없습니다."),
    ALREADY_LIKED(StatusCode.CONFLICT, "P003", "이미 좋아요를 누른 게시글입니다.")
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
