package kr.co.yournews.infra.openai;

import kr.co.yournews.infra.openai.dto.ChatDto;
import kr.co.yournews.infra.openai.dto.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeSummaryClient {
    private final OpenAIChatClient openAIChatClient;

    private static final String SYSTEM_ROLE_MESSAGE =
            "너는 대학 공지를 요약하는 어시스턴트야.";

    private static final String MESSAGE_PREFIX =
            "아래 제목과 본문만 근거로 **아주 간단히** 요약 답변만 작성하세요. " +
                    "반드시 3줄, 각 줄 끝에는 <br/> 태그를 붙이세요. " +
                    "답변은 존댓말(~합니다, ~됩니다)만 사용하세요. " +
                    "마크다운, 숫자 목록, 불릿, 이모지, 특수문자 절대 쓰지 말고 " +
                    "추가 설명 없이 요약 결과만 출력하세요. " +
                    "정보가 부족하면 '내용 부족, 직접 확인 필요'라고만 답하세요.";

    /**
     * 제목과 내용을 기반으로 OpenAI에게 내용 요약 요청을 보내고, 응답으로 받은 요약본을 반환
     *
     * @param title   : 게시글 제목
     * @param content : 게시글 내용
     * @return : GPT가 분류한 키워드 문자열
     */
    public String requestNewsSummary(String title, String content) {
        log.info("[GPT 내용 요약 요청] title: {}", title);

        String prompt = MESSAGE_PREFIX + "\n" +
                "제목: " + title + "\n" +
                "본문: " + content + "\n";

        Message system = new Message("system", SYSTEM_ROLE_MESSAGE);
        Message user = new Message("user", prompt);

        ChatDto.Response response = openAIChatClient.askQuestion(List.of(system, user));

        String summary = response.choices().get(0).message().content();

        log.info("[GPT 내용 요약 완료] title: {}", title);
        return summary;
    }
}
