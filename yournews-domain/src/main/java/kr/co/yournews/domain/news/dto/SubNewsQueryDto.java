package kr.co.yournews.domain.news.dto;

import kr.co.yournews.domain.news.type.KeywordType;

import java.util.List;

public record SubNewsQueryDto(
        String newsName,
        List<KeywordType> keywordTypes
) {
}
