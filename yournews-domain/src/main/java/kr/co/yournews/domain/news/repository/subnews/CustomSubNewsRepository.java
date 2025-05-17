package kr.co.yournews.domain.news.repository.subnews;

import kr.co.yournews.domain.news.dto.SubNewsQueryDto;
import kr.co.yournews.domain.news.dto.UserKeywordDto;

import java.util.List;

public interface CustomSubNewsRepository {
    List<UserKeywordDto> findUserKeywordsByUserIds(List<Long> userIds);
    List<SubNewsQueryDto> findSubNewsWithKeywordsByUserId(Long userId);
}
