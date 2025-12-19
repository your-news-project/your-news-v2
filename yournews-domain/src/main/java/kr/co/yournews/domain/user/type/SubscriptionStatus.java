package kr.co.yournews.domain.user.type;

public enum SubscriptionStatus {
    ACTIVE,                         // 정상 구독 (자동갱신 on)
    ACTIVE_CANCEL_AT_PERIOD_END,    // 이번 기간까지만 유효 (자동갱신 off)
    GRACE,                          // 결제 실패 유예기간
    BILLING_RETRY,                  // 결제 실패 재시도 중
    EXPIRED,                        // 만료
    REFUNDED                        // 환불
}
