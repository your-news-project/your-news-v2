package kr.co.yournews.domain.post.repository;

import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.type.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomPostRepository {
    Page<PostQueryDto.Summary> findAllByCategory(Category category, Pageable pageable);
    Optional<PostQueryDto.Details> findDetailsById(Long id);
}
