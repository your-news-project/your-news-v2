package kr.co.yournews.domain.post.service;

import kr.co.yournews.domain.post.entity.Post;
import kr.co.yournews.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }
}
