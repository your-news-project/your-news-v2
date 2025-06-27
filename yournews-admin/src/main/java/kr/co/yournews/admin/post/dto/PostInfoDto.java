package kr.co.yournews.admin.post.dto;

import kr.co.yournews.domain.post.dto.PostQueryDto;

import java.time.LocalDateTime;

public class PostInfoDto {

    public record Summary(
            Long id,
            String title,
            String nickname,
            LocalDateTime createdAt,
            Long likeCount
    ) {
        public static Summary from(PostQueryDto.Summary dto) {
            return new Summary(dto.id(), dto.title(), dto.nickname(), dto.createdAt(), dto.likeCount());
        }
    }

    public record Details(
            Long id,
            String title,
            String content,
            String nickname,
            LocalDateTime createdAt,
            Long userId,
            Long likeCount
    ) {
        public static Details from(PostQueryDto.DetailsForAdmin dto) {
            return new Details(dto.id(), dto.title(), dto.content(), dto.nickname(),
                    dto.createdAt(), dto.userId(), dto.likeCount());
        }
    }
}
