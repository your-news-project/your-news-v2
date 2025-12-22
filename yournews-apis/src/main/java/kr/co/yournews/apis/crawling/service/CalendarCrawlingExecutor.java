package kr.co.yournews.apis.crawling.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.yournews.apis.calendar.dto.CalendarRaw;
import kr.co.yournews.infra.crawling.CrawlingProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarCrawlingExecutor {
    private final CrawlingProcessor crawlingProcessor;
    private final ObjectMapper objectMapper;

    private static final String URL = "https://www.yu.ac.kr/main/bachelor/calendar.do?mode=calendar&srYear=";

    /**
     * 지정한 연도의 학사 일정을 크롤링하는 메서드.
     *
     * @param year : 크롤링 대상 연도
     * @return 해당 연도의 학사 일정 목록
     */
    public List<CalendarRaw> execute(int year) {
        try {
            log.info("[학사 일정 크롤링 시작] year: {}", year);
            String url = URL + year;

            Document doc = crawlingProcessor.fetch(url);

            String jsonArray = extractCalDataJsonArray(doc)
                    .orElseThrow(() -> new IllegalStateException("calData를 찾지 못했습니다."));

            return objectMapper.readValue(jsonArray, new TypeReference<List<CalendarRaw>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("학사일정 크롤링 실패 year=" + year, e);
        }
    }

    /**
     * HTML 문서 내 script 태그를 순회하며 "var calData"의 JSON 배열을 추출하는 메서드
     *
     * @param doc : 크롤링된 HTML 문서
     * @return : calData JSON 배열 문자열
     */
    private Optional<String> extractCalDataJsonArray(Document doc) {
        for (Element script : doc.select("script")) {
            String data = script.data();
            if (data.contains("var calData")) {
                return Optional.of(extractArrayLiteral(data, "var calData"));
            }
        }
        return Optional.empty();
    }

    /**
     * script 태그 내부 문자열에서 지정한 변수에 할당된 배열 부분을 추출하는 메서드
     *
     * @param scriptText : script 태그 내부 전체 문자열
     * @param varName    : 배열을 보유한 변수명
     * @return : 배열 리터럴 문자열
     */
    private String extractArrayLiteral(String scriptText, String varName) {
        int idx = scriptText.indexOf(varName);
        if (idx < 0) throw new IllegalArgumentException("varName not found: " + varName);

        int start = scriptText.indexOf('[', idx);
        if (start < 0) throw new IllegalArgumentException("array '[' not found");

        int end = scriptText.indexOf("];", start);
        if (end < 0) throw new IllegalArgumentException("array end '];' not found");

        return scriptText.substring(start, end + 1);
    }
}
