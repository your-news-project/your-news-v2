package kr.co.yournews.common.sentry;

import io.sentry.Sentry;
import io.sentry.SentryLevel;

import java.util.Map;

/**
 * Sentry로 보낼 이벤트만 캡처하는 유틸.
 * - beforeSend에서 tag("sentry")="1" 인 이벤트만 허용하도록 맞춰서 사용.
 */
public class SentryCapture {

    /**
     * WARNING 레벨 메시지 이벤트 전송.
     *
     * @param feature 기능/도메인 구분값 ("jwt", "subscription", "crawling")
     * @param tags    검색/필터링용 태그 (platform, stage, reason ...)
     * @param extras  상세 컨텍스트 (bodyLength, retryCount ...)
     * @param message 메시지 템플릿
     */
    public static void warn(
            String feature,
            Map<String, String> tags,
            Map<String, Object> extras,
            String message
    ) {
        Sentry.withScope(scope -> {
            scope.setLevel(SentryLevel.WARNING);

            // beforeSend에서 허용시키기 위한 explicit tag
            scope.setTag("sentry", "1");

            // 공통 tags
            scope.setTag("feature", feature);
            if (tags != null) tags.forEach((k, v) -> {
                if (v != null) scope.setTag(k, v);
            });

            // 공통 extras
            if (extras != null) {
                extras.forEach((k, v) -> {
                    if (v != null) {
                        scope.setExtra(k, String.valueOf(v));
                    }
                });
            }

            scope.setExtra("msg", message);
            Sentry.captureMessage(message);
        });
    }

    public static void warn(String feature, Map<String, String> tags, String message) {
        warn(feature, tags, null, message);
    }
}