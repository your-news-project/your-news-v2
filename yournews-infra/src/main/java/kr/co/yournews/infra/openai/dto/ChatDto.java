package kr.co.yournews.infra.openai.dto;

import lombok.Builder;

import java.util.List;

public class ChatDto {

    @Builder
    public record Request(
            String model,
            List<Message> messages
    ) { }

    public record Response(
            List<Choice> choices
    ) {
        public record Choice(
                int index,
                Message message
        ) { }
    }
}
