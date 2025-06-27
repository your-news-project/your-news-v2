package kr.co.yournews.domain.post.repository;

import kr.co.yournews.domain.post.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);
    void deleteByUser_IdAndPost_Id(Long userId, Long postId);

    @Modifying
    @Query("DELETE FROM post_like p WHERE p.user.id IN :userIds")
    void deleteAllByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("DELETE FROM post_like p WHERE p.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
