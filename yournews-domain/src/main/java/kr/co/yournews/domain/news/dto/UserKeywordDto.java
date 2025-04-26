package kr.co.yournews.domain.news.dto;

import kr.co.yournews.domain.news.type.KeywordType;

public record UserKeywordDto(
        Long userId,
        KeywordType keywordType
) {
}
