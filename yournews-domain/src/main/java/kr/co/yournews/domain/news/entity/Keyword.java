package kr.co.yournews.domain.news.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kr.co.yournews.domain.news.type.KeywordType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "keyword")
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_type", nullable = false)
    private KeywordType keywordType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_news_id")
    private SubNews subNews;

    @Builder
    public Keyword(KeywordType keywordType, SubNews subNews) {
        this.keywordType = keywordType;
        this.subNews = subNews;
    }
}
