package kr.co.yournews.common.response.exception;

import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final BaseErrorType errorType;
}
