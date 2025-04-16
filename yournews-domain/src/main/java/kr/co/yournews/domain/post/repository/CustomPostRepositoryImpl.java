package kr.co.yournews.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.entity.QPost;
import kr.co.yournews.domain.post.type.Category;
import kr.co.yournews.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {
    private final JPAQueryFactory jpaQueryFactory;

    private final QUser user = QUser.user;
    private final QPost post = QPost.post;

    @Override
    public Page<PostQueryDto.Summary> findAllByCategory(Category category, Pageable pageable) {
        List<PostQueryDto.Summary> content = jpaQueryFactory
                .select(Projections.constructor(
                        PostQueryDto.Summary.class,
                        post.id,
                        post.title,
                        user.nickname,
                        post.createdAt
                ))
                .from(post)
                .join(post.user, user)
                .where(post.category.eq(category))
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(post.count())
                .from(post)
                .where(post.category.eq(category))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<PostQueryDto.Details> findDetailsById(Long id) {
        PostQueryDto.Details details = jpaQueryFactory
                .select(Projections.constructor(
                        PostQueryDto.Details.class,
                        post.id,
                        post.title,
                        post.content,
                        user.nickname,
                        post.createdAt
                ))
                .from(post)
                .join(post.user, user)
                .where(post.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(details);
    }
}
