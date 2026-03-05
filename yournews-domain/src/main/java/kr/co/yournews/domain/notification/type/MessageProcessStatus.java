package kr.co.yournews.domain.notification.type;

public enum MessageProcessStatus {
    PROCESSING,
    RETRY_PENDING,
    SUCCEEDED,
    FAILED_RETRY_EXHAUSTED,
    FAILED_PERMANENT
}
