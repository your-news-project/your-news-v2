package kr.co.yournews.domain.post.repository;

import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.type.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {
    Optional<Post> findTopByCategoryOrderByCreatedAtDesc(Category category);
}
