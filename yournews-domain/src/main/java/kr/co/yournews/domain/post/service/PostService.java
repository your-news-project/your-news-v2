package kr.co.yournews.domain.post.service;

import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.repository.PostRepository;
import kr.co.yournews.domain.post.type.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Long save(Post post) {
        return postRepository.save(post).getId();
    }

    public Optional<Post> readById(Long id) {
        return postRepository.findById(id);
    }

    public Optional<PostQueryDto.Details> readDetailsById(Long postId, Long userId) {
        return postRepository.findDetailsById(postId, userId);
    }

    public Optional<PostQueryDto.DetailsForAdmin> readDetailsById(Long postId) {
        return postRepository.findDetailsById(postId);
    }

    public Page<PostQueryDto.Summary> readByCategory(Category category, Pageable pageable) {
        return postRepository.findAllByCategory(category, pageable);
    }

    public Optional<Post> readTopByCategoryOrderByCreatedAtDesc(Category category) {
        return postRepository.findTopByCategoryOrderByCreatedAtDesc(category);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }
}
