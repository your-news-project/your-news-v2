package kr.co.yournews.domain.post.dto;

import java.time.LocalDateTime;

public class PostQueryDto {
    
    public record Summary(
            Long id,
            String title,
            String nickname,
            LocalDateTime createdAt,
            Long likeCount
    ) { }

    public record Details(
            Long id,
            String title,
            String content,
            String nickname,
            LocalDateTime createdAt,
            Long userId,
            Long likeCount,
            Boolean liked
    ) { }

    public record DetailsForAdmin(
            Long id,
            String title,
            String content,
            String nickname,
            LocalDateTime createdAt,
            Long userId,
            Long likeCount
    ) { }
}
