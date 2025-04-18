package kr.co.yournews.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.yournews.domain.post.dto.PostQueryDto;
import kr.co.yournews.domain.post.entity.QPost;
import kr.co.yournews.domain.post.entity.QPostLike;
import kr.co.yournews.domain.post.type.Category;
import kr.co.yournews.domain.user.entity.QUser;
import kr.co.yournews.domain.user.entity.User;
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
    private final QPostLike postLike = QPostLike.postLike;

    @Override
    public Page<PostQueryDto.Summary> findAllByCategory(Category category, Pageable pageable) {
        List<PostQueryDto.Summary> content = jpaQueryFactory
                .select(Projections.constructor(
                        PostQueryDto.Summary.class,
                        post.id,
                        post.title,
                        user.nickname.coalesce(User.UNKNOWN_NICKNAME),
                        post.createdAt,
                        JPAExpressions
                                .select(postLike.count())
                                .from(postLike)
                                .where(postLike.post.id.eq(post.id))
                ))
                .from(post)
                .leftJoin(post.user, user)
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
    public Optional<PostQueryDto.Details> findDetailsById(Long postId, Long userId) {
        PostQueryDto.Details details = jpaQueryFactory
                .select(Projections.constructor(
                        PostQueryDto.Details.class,
                        post.id,
                        post.title,
                        post.content,
                        user.nickname.coalesce(User.UNKNOWN_NICKNAME),
                        post.createdAt,
                        post.userId,
                        JPAExpressions
                                .select(postLike.count())
                                .from(postLike)
                                .where(postLike.post.id.eq(post.id)),
                        JPAExpressions
                                .selectOne()
                                .from(postLike)
                                .where(
                                        postLike.post.id.eq(post.id)
                                                .and(postLike.user.id.eq(userId))
                                ).exists()
                ))
                .from(post)
                .leftJoin(post.user, user)
                .where(post.id.eq(postId))
                .fetchOne();

        return Optional.ofNullable(details);
    }
}
