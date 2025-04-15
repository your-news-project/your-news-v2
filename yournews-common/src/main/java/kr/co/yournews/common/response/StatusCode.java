package kr.co.yournews.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * HTTP 상태 코드
 */
@Getter
@RequiredArgsConstructor
public enum StatusCode {
    OK(200),

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),

    INTERNAL_SERVER_ERROR(500);

    private final int code;
}
