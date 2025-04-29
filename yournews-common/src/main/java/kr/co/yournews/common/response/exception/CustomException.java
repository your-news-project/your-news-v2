package kr.co.yournews.common.response.exception;

import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class CustomException extends RuntimeException {
    private final BaseErrorType errorType;
    private final Object data;

    public CustomException(BaseErrorType errorType) {
        this.errorType = errorType;
        this.data = null;
    }

    public CustomException(BaseErrorType errorType, Object data) {
        this.errorType = errorType;
        this.data = data;
    }
}
