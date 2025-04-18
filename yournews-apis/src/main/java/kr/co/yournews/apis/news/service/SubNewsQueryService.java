package kr.co.yournews.apis.news.service;

import kr.co.yournews.apis.news.dto.SubNewsDto;
import kr.co.yournews.domain.news.service.SubNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubNewsQueryService {
    private final SubNewsService subNewsService;

    /**
     * 사용자가 구독 중인 모든 뉴스 목록을 조회
     *
     * @param userId : 사용자 pk
     * @return : 사용자가 구독한 뉴스 목록 DTO 리스트
     */
    public List<SubNewsDto.Response> getAllSubNews(Long userId) {
        return subNewsService.readByUserId(userId)
                .stream().map(SubNewsDto.Response::from)
                .toList();
    }
}
