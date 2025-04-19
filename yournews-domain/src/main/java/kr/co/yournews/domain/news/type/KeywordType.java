package kr.co.yournews.domain.news.type;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.exception.NewsErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum KeywordType {
    SCHOLARSHIP("장학금 및 학자금 지원"),
    JOB("취업"),
    EDUCATION("교육 및 강좌"),
    WELFARE("학생복지"),
    RESEARCH("연구 및 프로젝트 참여"),
    EVENT("행사 및 설명회");

    private final String label;

    private static final Map<String, KeywordType> LABEL_MAP = new HashMap<>();

    static {
        for (KeywordType type : values()) {
            LABEL_MAP.put(type.label, type);
        }
    }

    /**
     * 전달된 문자열 라벨에 해당하는 KeywordType enum을 반환
     *
     * @param label : 클라이언트가 전달한 키워드 문자열
     * @return : 해당 키워드에 대응하는 KeywordType enum
     * @throws CustomException INVALID_KEYWORD : 유효하지 않은 키워드일 경우 예외 발생
     */
    public static KeywordType fromLabel(String label) {
        KeywordType keywordType = LABEL_MAP.get(label);
        if (keywordType == null) {
            throw new CustomException(NewsErrorType.INVALID_KEYWORD);
        }
        return keywordType;
    }
}
