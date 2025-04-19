package kr.co.yournews.domain.news.repository;

import kr.co.yournews.domain.news.entity.SubNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubNewsRepository extends JpaRepository<SubNews, Long> {
    List<SubNews> findByUser_Id(Long userId);

    @Modifying
    @Query("DELETE FROM sub_news s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
