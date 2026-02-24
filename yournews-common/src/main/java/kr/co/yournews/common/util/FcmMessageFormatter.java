package kr.co.yournews.common.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * FCM 메시지 본문 포맷 유틸리티 클래스
 */
public final class FcmMessageFormatter {

    private FcmMessageFormatter() {
    }

    public static String formatTitles(List<String> titles) {
        return IntStream.range(0, titles.size())
                .mapToObj(i -> String.format("%d) %s", i + 1, titles.get(i)))
                .collect(Collectors.joining("\n"));
    }
}
