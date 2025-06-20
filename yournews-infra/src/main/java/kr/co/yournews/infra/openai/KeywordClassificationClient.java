package kr.co.yournews.infra.openai;

import kr.co.yournews.infra.openai.dto.ChatDto;
import kr.co.yournews.infra.openai.dto.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordClassificationClient {
    private final OpenAIChatClient openAIChatClient;

    private static final String SYSTEM_ROLE_MESSAGE =
            "너는 대학 공지사항 키워드 분류기야.";

    private static final String MESSAGE_PREFIX =
            "키워드는 '장학금 및 학자금 지원', '취업', '교육 및 강좌', '학생복지', '연구 및 프로젝트 참여', '행사 및 설명회'가 있어." +
                    " 다음 제목의 키워드를 정확히 말해줘. 가장 유사한 하나의 키워드만 말해줘. 예를 들자면 '취업' 이렇게 말해. 다른 말은 하지마. \n제목: ";

    /**
     * 제목을 기반으로 OpenAI에게 키워드 분류 요청을 보내고, 응답으로 받은 키워드를 정제하여 반환
     *
     * @param title : 게시글 제목
     * @return : GPT가 분류한 키워드 문자열
     */
    public String requestKeyword(String title) {
        String prompt = MESSAGE_PREFIX + title;

        Message system = new Message("system", SYSTEM_ROLE_MESSAGE);
        Message user = new Message("user", prompt);

        ChatDto.Response response = openAIChatClient.askQuestion(List.of(system, user));

        String keyword = response.choices().get(0).message().content();
        return keyword.replaceAll("^[\"']|[\"']$|\\.$", "").trim();
    }
}
