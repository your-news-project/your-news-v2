package kr.co.yournews.domain.post.repository;

import kr.co.yournews.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
