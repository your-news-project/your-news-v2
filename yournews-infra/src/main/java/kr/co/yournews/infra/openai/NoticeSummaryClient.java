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
            "아래 제공된 제목과 본문 텍스트만 근거로 3~5줄로 요약해줘. " +
                    "각 줄은 마침표로 끝내줘. " +
                    "마크다운, 이모지, 특수문자, 불릿포인트는 사용하지 말고 순수 텍스트만 작성해줘. " +
                    "만약 제목이나 본문에 정보가 거의 없거나 요약할 수 없다면: '내용 부족, 직접 확인 필요' 라고만 답해줘.";

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
