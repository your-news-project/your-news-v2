package kr.co.yournews.domain.news.repository.subnews;

import kr.co.yournews.domain.news.dto.UserKeywordDto;

import java.util.List;

public interface CustomSubNewsRepository {
    List<UserKeywordDto> findUserKeywordsByUserIds(List<Long> userIds);
}
