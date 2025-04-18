package kr.co.yournews.domain.post.service;

import kr.co.yournews.domain.post.entity.PostLike;
import kr.co.yournews.domain.post.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;

    public void save(PostLike postLike) {
        postLikeRepository.save(postLike);
    }

    public void deleteByUserIdAndPostId(Long userId, Long postId) {
        postLikeRepository.deleteByUser_IdAndPost_Id(userId, postId);
    }

    public boolean existsByUserIdAndPostId(Long userId, Long poseId) {
        return postLikeRepository.existsByUser_IdAndPost_Id(userId, poseId);
    }
}
