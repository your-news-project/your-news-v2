package kr.co.yournews.common.response.error.type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Common ErrorCode: Gxxx
 */
@RequiredArgsConstructor
public enum GlobalErrorType implements BaseErrorType {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 에러입니다. 관리자에게 문의하세요."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "G002", "유효성 검증에 실패하였습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
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
