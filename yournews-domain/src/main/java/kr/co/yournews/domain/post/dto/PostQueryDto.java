package kr.co.yournews.domain.post.dto;

import java.time.LocalDateTime;

public class PostQueryDto {
    
    public record Summary(
            Long id,
            String title,
            String nickname,
            LocalDateTime createdAt
    ) { }

    public record Details(
            Long id,
            String title,
            String content,
            String nickname,
            LocalDateTime createdAt
    ) { }
}
