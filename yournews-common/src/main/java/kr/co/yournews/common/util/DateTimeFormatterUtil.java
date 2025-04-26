package kr.co.yournews.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 포맷 변환 유틸리티 클래스
 * - 문자열을 LocalDate로 변환하는 기능 제공
 */
public class DateTimeFormatterUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 주어진 문자열을 LocalDate로 파싱
     *
     * @param date 변환할 날짜 문자열 (형식: yyyy-MM-dd)
     * @return 변환된 LocalDate 객체
     */
    public static LocalDate parseToLocalDateTime(String date) {
        return LocalDate.parse(date, formatter);
    }
}
