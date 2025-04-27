package kr.co.yournews.domain.news.repository.keyword;

import kr.co.yournews.domain.news.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long>, CustomKeywordRepository {
}
