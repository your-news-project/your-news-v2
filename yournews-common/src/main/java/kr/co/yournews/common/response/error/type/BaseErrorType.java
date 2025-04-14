package kr.co.yournews.common.response.error.type;

import kr.co.yournews.common.response.exception.StatusCode;

public interface BaseErrorType {
    StatusCode getStatus();
    String getCode();
    String getMessage();
}