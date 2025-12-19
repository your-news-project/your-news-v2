package kr.co.yournews.domain.user.exception;

import kr.co.yournews.common.response.StatusCode;
import kr.co.yournews.common.response.error.type.BaseErrorType;
import lombok.RequiredArgsConstructor;

/**
 * Subscription ErrorCode: Sxxx
 */
@RequiredArgsConstructor
public enum SubscriptionErrorType implements BaseErrorType {

    SUBSCRIPTION_OWNED_BY_ANOTHER_USER(StatusCode.CONFLICT, "S001", "이미 다른 계정에 연결된 구독입니다."),
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
