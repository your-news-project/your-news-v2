package kr.co.yournews.admin.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.type.Category;
import kr.co.yournews.domain.user.entity.User;

public class PostDto {

    public record Request(
            @NotBlank(message = "제목은 필수 입력입니다.")
            @Size(max = 50, message = "제목은 최대 50자까지 작성할 수 있습니다.")
            String title,

            @NotBlank(message = "내용은 필수 입력입니다.")
            String content
    ) {
        public Post toEntity(User user) {
            return Post.builder()
                    .title(title)
                    .content(content)
                    .category(Category.NOTICE)
                    .user(user)
                    .build();
        }
    }

    public record Response(
            Long id
    ) {
        public static Response of(Long id) {
            return new Response(id);
        }
    }
}
