package kr.co.yournews.common.response.error.type;

import org.springframework.http.HttpStatus;

public interface BaseErrorType {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}