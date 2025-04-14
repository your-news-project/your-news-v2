package kr.co.yournews.common.response.exception;

import kr.co.yournews.common.response.error.type.BaseErrorType;

public class BlackListException extends RuntimeException {

    public BlackListException(BaseErrorType baseErrorType) {
        super(baseErrorType.getMessage());
    }
}
