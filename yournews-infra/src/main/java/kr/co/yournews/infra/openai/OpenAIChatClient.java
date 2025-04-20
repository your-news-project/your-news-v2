package kr.co.yournews.infra.openai;

import kr.co.yournews.infra.openai.dto.ChatDto;
import kr.co.yournews.infra.openai.dto.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OpenAIChatClient {
    private final RestTemplate restTemplate;

    @Value("${openai.api.url}")
    private String openAiUrl;

    @Value("${openai.api.model}")
    private String openAiModel;

    public OpenAIChatClient(@Qualifier("openAiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * OpenAI(ChatGPT) API에 메시지를 전송하여 응답을 받아오는 메서드
     *
     * @param messages : OpenAI에 전달할 시스템/유저 메시지 목록
     * @return : OpenAI(ChatGPT) 응답 결과 (ChatDto.Response)
     */
    public ChatDto.Response askQuestion(List<Message> messages) {
        ChatDto.Request request = ChatDto.Request.builder()
                .model(openAiModel)
                .messages(messages)
                .build();

        HttpEntity<ChatDto.Request> entity = new HttpEntity<>(request);
        ResponseEntity<ChatDto.Response> response = restTemplate.exchange(
                openAiUrl,
                HttpMethod.POST,
                entity,
                ChatDto.Response.class
        );

        return response.getBody();
    }
}
