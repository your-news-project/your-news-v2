package kr.co.yournews.domain.notification.exception;

import kr.co.yournews.common.response.StatusCode;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.RequiredArgsConstructor;

/**
 * Notification ErrorCode: NTxxx
 */
@RequiredArgsConstructor
public enum NotificationErrorType implements BaseErrorType {

    NOT_FOUND(StatusCode.NOT_FOUND, "NT001", "존재하지 않는 알림입니다.")
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
