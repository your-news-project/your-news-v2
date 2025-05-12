package kr.co.yournews.domain.news.type;

import kr.co.yournews.common.response.exception.CustomException;
import kr.co.yournews.domain.news.exception.NewsErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum College {

    HUMANITIES("인문대학"),
    NATURAL_SCIENCE("자연과학대학"),
    ENGINEERING("공과대학"),
    DIGITAL_CONVERGENCE("디지털융합대학"),
    SOCIAL_SCIENCE("사회과학대학"),
    BUSINESS("경영대학"),
    MEDICINE("의과대학"),
    PHARMACY("약학대학"),
    LIFE_APPLIED_SCIENCE("생명응용과학대학"),
    HUMAN_ECOLOGY("생활과학대학"),
    LAW("사범대학"),
    ARTS("예술대학"),
    GLOBAL_HUMANITIES("글로벌인재대학"),
    CHUNMA_COLLEGE("천마학부대학"),
    ETC("기타");

    private final String label;

    private static final Map<String, College> LABEL_MAP = new HashMap<>();

    static {
        for (College type : values()) {
            LABEL_MAP.put(type.label, type);
        }
    }

    /**
     * 전달된 문자열 라벨에 해당하는 College enum을 반환
     *
     * @param label : 클라이언트가 전달한 대학 문자열
     * @return : 해당 키워드에 대응하는 College enum
     * @throws CustomException INVALID_COLLEGE : 유효하지 않은 대학일 경우 예외 발생
     */
    public static College fromLabel(String label) {
        College college = LABEL_MAP.get(label);
        if (college == null) {
            throw new CustomException(NewsErrorType.INVALID_COLLEGE);
        }
        return college;
    }
}
