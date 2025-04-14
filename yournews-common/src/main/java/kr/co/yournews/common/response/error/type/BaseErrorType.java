package kr.co.yournews.common.response.error.type;

import kr.co.yournews.common.response.StatusCode;

public interface BaseErrorType {
    StatusCode getStatus();
    String getCode();
    String getMessage();
}