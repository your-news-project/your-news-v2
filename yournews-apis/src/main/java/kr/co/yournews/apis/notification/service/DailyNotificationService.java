package kr.co.yournews.apis.notification.service;

import kr.co.yournews.apis.notification.dto.DailyNewsDto;
import kr.co.yournews.infra.redis.RedisRepository;
import kr.co.yournews.infra.redis.util.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DailyNotificationService {
    private final RedisRepository redisRepository;

    /**
     * 일간 알림을 위한 소식 이름을 기반으로 제목과 URL 정보를 Redis에 저장하는 메서드
     *
     * @param newsName : 소식 이름
     * @param titles   : 새로운 소식 제목 리스트
     * @param urls     : 새로운 소식 URL 리스트
     */
    public void saveNewsInfo(String newsName, List<String> titles, List<String> urls) {
        String key = RedisConstants.NEWS_INFO_KEY_PREFIX + newsName;
        List<DailyNewsDto> dailyNewsDtos = IntStream.range(0, titles.size())
                .mapToObj(i -> DailyNewsDto.of(titles.get(i), urls.get(i)))
                .toList();

        redisRepository.setListAll(key, dailyNewsDtos);
    }

    /**
     * Redis에 저장된 일간 뉴스 정보를 조회 메서드
     * - 조회 후 삭제
     *
     * @param newsName 소식 이름
     * @return : 저장되어 있던 DailyNewsDto 리스트
     */
    public List<DailyNewsDto> getAllNewsInfo(String newsName) {
        String key = RedisConstants.NEWS_INFO_KEY_PREFIX + newsName;
        List<DailyNewsDto> newsInfoList = redisRepository.getList(key, DailyNewsDto.class);

        redisRepository.del(key);

        return newsInfoList;
    }
}
