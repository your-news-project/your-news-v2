package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.dto.NotificationRankingDto;
import kr.co.yournews.infra.redis.RedisRepository;
import kr.co.yournews.infra.redis.util.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationRankingService {
    private final RedisRepository redisRepository;

    /**
     * 소식 이름(newsName)을 기준으로 Redis ZSet 내 점수를 증가시키는 메서드
     * - ZSet 구조를 사용하여 금일 소식별 알림 수를 랭킹 형태로 저장
     * - 점수는 해당 소식의 새로 저장된 게시글 수(count)이며, 자정까지 유효한 TTL을 설정
     *
     * @param newsName : 소식 이름 (ZSet의 key 멤버로 사용)
     * @param count    : 소식에 해당하는 게시글 수 (ZSet 점수로 누적됨)
     */
    public void incrementNewsRanking(String newsName, int count) {
        redisRepository.incrementZSetScore(
                RedisConstants.NEWS_RANKING_KEY_PREFIX,
                newsName,
                count,
                getDurationUntilMidnight()
        );
    }

    /**
     * 현재 시간부터 자정까지 남은 Duration을 계산하는 유틸 메서드
     *
     * @return 현재 시점부터 오늘 자정까지의 Duration
     */
    private Duration getDurationUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight);
    }

    /**
     * Redis에 저장된 일간 소식 랭킹 정보를 조회하는 메서드
     *
     * @return 소식 이름과 점수를 포함한 랭킹 DTO 리스트
     */
    public List<NotificationRankingDto> getNewsRanking() {
        return redisRepository.getZSetWithScore(
                RedisConstants.NEWS_RANKING_KEY_PREFIX,
                NotificationRankingDto::new
        );
    }
}
