package kr.co.yournews.domain.news.repository.keyword;

import kr.co.yournews.domain.news.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long>, CustomKeywordRepository {

    @Modifying
    @Query("DELETE FROM keyword k WHERE k.subNews.user.id IN :userIds")
    void deleteAllByUserIds(@Param("userIds") List<Long> userIds);
}
