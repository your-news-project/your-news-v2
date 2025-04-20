package kr.co.yournews.infra.openai.dto;

public record Message(
        String role,
        String content
) {
}
