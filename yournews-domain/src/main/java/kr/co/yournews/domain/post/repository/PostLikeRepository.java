package kr.co.yournews.domain.post.repository;

import kr.co.yournews.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);
    void deleteByUser_IdAndPost_Id(Long userId, Long postId);
}
