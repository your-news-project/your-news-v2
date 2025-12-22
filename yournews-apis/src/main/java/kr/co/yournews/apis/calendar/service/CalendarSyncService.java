package kr.co.yournews.apis.calendar.service;

import kr.co.yournews.apis.calendar.dto.CalendarRaw;
import kr.co.yournews.apis.crawling.service.CalendarCrawlingExecutor;
import kr.co.yournews.domain.calendar.entity.Calendar;
import kr.co.yournews.domain.calendar.service.CalendarService;
import kr.co.yournews.domain.calendar.type.CalendarType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarSyncService {
    private final CalendarCrawlingExecutor calendarCrawlingExecutor;
    private final CalendarService calendarService;

    /**
     * 학사 일정을 크롤링하여 동기하는 메서드.
     * - 동기화 대상는 현재 연도와 다음연도
     * - 추가된 일정, 사라진 일정, 변경된 일정 등을 파악하여 동기화 진행
     */
    @Transactional
    public void syncCalendars() {
        int currentYear = LocalDate.now().getYear();
        Set<Integer> targetYears = Set.of(currentYear, currentYear + 1);

        List<CalendarRaw> filteredCalendars = crawlAndFilterCalendars(targetYears);
        List<Calendar> existingCalendars = calendarService.readAllByYears(targetYears);

        List<Calendar> calendarsToInsert = new ArrayList<>();
        List<Long> calendarIdsToDelete = new ArrayList<>();

        collectSyncChanges(filteredCalendars, existingCalendars, calendarsToInsert, calendarIdsToDelete);
        applyChanges(calendarIdsToDelete, calendarsToInsert);

        log.info("[학사 일정] 동기화 완료. 크롤링 개수: {}", filteredCalendars.size());
    }

    /**
     * 대상 연도(현재 연도 + 다음 연도)를 크롤링하고, 적합한 일정을 필터링하는 메서드.
     *
     * @param targetYears : 동기화 대상 연도 집합 (현재 연도 + 다음 연도)
     * @return : 필터된 크롤링 결과 목록
     */
    private List<CalendarRaw> crawlAndFilterCalendars(Set<Integer> targetYears) {
        return targetYears.stream()
                .flatMap(year -> calendarCrawlingExecutor.execute(year).stream())
                .filter(dto -> isWithinTargetYears(dto, targetYears))
                .filter(this::isDisplayableEvent)
                .toList();
    }

    /**
     * 크롤링된 일정이 동기화 대상 연도에 포함되는지 검사하는 메서드.
     * - 일정의 시작과 끝이 대상 연도에 포함되는지 확인
     *
     * @param raw         : 크롤링된 일정
     * @param targetYears : 동기화 대상 연도 집합 (현재 연도 + 다음 연도)
     * @return : 대상 연도에 포함되면 true
     */
    private boolean isWithinTargetYears(CalendarRaw raw, Set<Integer> targetYears) {
        LocalDate start = LocalDate.parse(raw.startDt());
        LocalDate end = LocalDate.parse(raw.endDt());

        return start.getYear() == end.getYear()
                && targetYears.contains(start.getYear());
    }

    /**
     * 불필요한 일정 판별하는 메서드.
     *
     * @param raw : 크롤링된 일정
     * @return : 표시 가능하면 true
     */
    private boolean isDisplayableEvent(CalendarRaw raw) {
        String text = raw.text();
        if (text == null || text.isBlank()) {
            return false;
        }

        if (text.startsWith("수업일수") || text.startsWith("수강지도")) {
            return false;
        }

        return !text.startsWith("학기개시일") || text.equals("학기개시일");
    }

    /**
     * 크롤링된 일정 목록과 기존 저장된 일정을 비교하여
     * 추가/수정/삭제 대상 일정을 분류하는 메서드.
     * - 신규 크롤링 일정이 기존에 없음 → 추가 대상
     * - articleNo는 같지만 내용이 변경 → 기존 삭제 후 재삽입
     * - 기존에는 있으나 이번 크롤링 결과에 없는 일정 → 삭제 대상
     *
     * @param filteredCalendars   : 크롤링 및 필터링을 통과한 일정 목록
     * @param existingCalendars   : DB에 저장된 기존 일정 목록
     * @param calendarsToInsert   : 신규 또는 수정되어 삽입될 일정 결과 리스트
     * @param calendarIdsToDelete : 삭제 대상 기존 일정 ID 리스트
     */
    private void collectSyncChanges(
            List<CalendarRaw> filteredCalendars,
            List<Calendar> existingCalendars,
            List<Calendar> calendarsToInsert,
            List<Long> calendarIdsToDelete
    ) {
        Map<Long, Calendar> existingByArticleNo = existingCalendars.stream()
                .collect(Collectors.toMap(Calendar::getArticleNo, Function.identity()));

        Set<Long> crawledArticleNos = new HashSet<>();

        for (CalendarRaw raw : filteredCalendars) {
            Long articleNo = raw.articleNo();

            // 중복 처리 방지
            if (!crawledArticleNos.add(articleNo)) {
                continue;
            }

            Calendar found = existingByArticleNo.get(articleNo);

            // 신규 일정
            if (found == null) {
                calendarsToInsert.add(toEntity(raw));
                continue;
            }

            // 변경된 일정
            if (isChanged(found, raw)) {
                calendarIdsToDelete.add(found.getId());
                calendarsToInsert.add(toEntity(raw));
            }
        }

        // 사라진 일정
        existingCalendars.stream()
                .filter(c -> !crawledArticleNos.contains(c.getArticleNo()))
                .map(Calendar::getId)
                .forEach(calendarIdsToDelete::add);
    }

    /**
     * 기존 엔티티와 크롤링 원본 간 변경 여부를 판단하는 메서드.
     *
     * @param existing : DB에 저장된 기존 일정
     * @param raw      : 크롤링된 일정
     * @return : 변경되었으면 true
     */
    private boolean isChanged(Calendar existing, CalendarRaw raw) {
        return !Objects.equals(existing.getTitle(), raw.text())
                || !Objects.equals(existing.getStartAt(), LocalDate.parse(raw.startDt()))
                || !Objects.equals(existing.getEndAt(), LocalDate.parse(raw.endDt()));
    }

    private Calendar toEntity(CalendarRaw raw) {
        return Calendar.builder()
                .title(raw.text())
                .articleNo(raw.articleNo())
                .startAt(LocalDate.parse(raw.startDt()))
                .endAt(LocalDate.parse(raw.endDt()))
                .type(CalendarType.ACADEMIC)
                .build();
    }

    /**
     * 변경사을 DB에 동기화하는 메서드.
     *
     * @param calendarIdsToDelete : 삭제할 Calendar의 id 리스트
     * @param calendarsToInsert   : 삽입할 Calendar 리스트
     */
    private void applyChanges(List<Long> calendarIdsToDelete, List<Calendar> calendarsToInsert) {
        if (!calendarIdsToDelete.isEmpty()) {
            calendarService.deleteAllByIdInBatch(calendarIdsToDelete);
        }
        if (!calendarsToInsert.isEmpty()) {
            calendarService.saveAll(calendarsToInsert);
        }
    }
}