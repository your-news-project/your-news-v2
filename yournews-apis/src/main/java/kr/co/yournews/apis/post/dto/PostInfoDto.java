package kr.co.yournews.apis.post.dto;

import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.entity.Post;

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
            Long likeCount,
            boolean liked
    ) {
        public static Details from(PostQueryDto.Details dto) {
            return new Details(dto.id(), dto.title(), dto.content(), dto.nickname(),
                    dto.createdAt(), dto.userId(), dto.likeCount(), dto.liked());
        }
    }

    public record Preview(
            Long id,
            String title
    ) {
        public static Preview from(Post post) {
            return new Preview(post.getId(), post.getTitle());
        }

        public static Preview empty() {
            return new Preview(null, null);
        }
    }
}
