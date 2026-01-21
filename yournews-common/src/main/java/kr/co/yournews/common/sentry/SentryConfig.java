package kr.co.yournews.common.sentry;

import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class SentryConfig {

    private static final Set<String> DROP_EXCEPTION_CLASSNAMES = Set.of(
            "io.jsonwebtoken.ExpiredJwtException"
    );

    @Bean
    public Sentry.OptionsConfiguration<SentryOptions> sentryOptionsConfiguration() {
        return options -> options.setBeforeSend(SentryConfig::beforeSend);
    }

    /**
     * 이벤트 전송 직전 필터링 로직
     *
     * @return : null(전송x) / event(전송o)
     */
    private static SentryEvent beforeSend(SentryEvent event, Hint hint) {
        Throwable t = event.getThrowable();

        // 드랍 대상 예외면 무조건 차단
        if (t != null && hasAnyCauseClassName(t, DROP_EXCEPTION_CLASSNAMES)) {
            return null;
        }

        // 명시적으로 보낸 이벤트만 허용
        String sentryTag = event.getTags() != null ? event.getTags().get("sentry") : null;
        if (!"1".equals(sentryTag)) {
            return null;
        }

        return event;
    }

    /**
     * 예외 클래스 확인
     */
    private static boolean hasAnyCauseClassName(Throwable t, Set<String> classNames) {
        Throwable cur = t;
        while (cur != null) {
            if (classNames.contains(cur.getClass().getName())) return true;
            cur = cur.getCause();
        }
        return false;
    }
}