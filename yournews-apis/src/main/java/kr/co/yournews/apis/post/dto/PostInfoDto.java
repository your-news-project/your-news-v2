package kr.co.yournews.apis.post.dto;

import kr.co.yournews.domain.post.dto.PostQueryDto;

import java.time.LocalDateTime;

public class PostInfoDto {

    public record Summary(
            Long id,
            String title,
            String nickname,
            LocalDateTime createdAt
    ) {
        public static Summary from(PostQueryDto.Summary dto) {
            return new Summary(dto.id(), dto.title(), dto.nickname(), dto.createdAt());
        }
    }

    public record Details(
            Long id,
            String title,
            String content,
            String nickname,
            LocalDateTime createdAt,
            Long userId
    ) {
        public static Details from(PostQueryDto.Details dto) {
            return new Details(dto.id(), dto.title(), dto.content(), dto.nickname(), dto.createdAt(), dto.userId());
        }
    }
}
