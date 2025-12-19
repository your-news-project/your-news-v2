package kr.co.yournews.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 날짜/시간 변환 유틸리티 클래스
 */
public final class DateTimeConvertUtil {

    /**
     * 한국 표준시 (KST)
     */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private DateTimeConvertUtil() {
    }

    /**
     * Epoch milliseconds 값을 KST 기준으로 변환
     *
     * @param epochMillis 변환할 epoch milliseconds
     * @return LocalDateTime로 변환된 값
     */
    public static LocalDateTime epochMillisToLocalDateTime(Long epochMillis) {
        if (epochMillis == null) return null;
        return Instant.ofEpochMilli(epochMillis)
                .atZone(KST)
                .toLocalDateTime();
    }
}
