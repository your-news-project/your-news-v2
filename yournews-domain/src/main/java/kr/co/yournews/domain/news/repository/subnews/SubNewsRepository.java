package kr.co.yournews.domain.news.repository.subnews;

import kr.co.yournews.domain.news.entity.SubNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubNewsRepository extends JpaRepository<SubNews, Long>, CustomSubNewsRepository {
    List<SubNews> findByUser_Id(Long userId);

    @Modifying
    @Query("DELETE FROM sub_news s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM sub_news s WHERE s.user.id IN :userIds")
    void deleteAllByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("DELETE FROM sub_news s WHERE s.news.id = :newsId")
    void deleteAllByNewsId(@Param("newsId") Long newsId);
}
