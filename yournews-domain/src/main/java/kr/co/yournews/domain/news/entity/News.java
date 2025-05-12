package kr.co.yournews.domain.news.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.co.yournews.domain.news.type.College;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "news")
public class News {

    private static final String YU_NEWS_NAME = "영대소식";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "college")
    private College college;

    @Builder
    public News(String name, String url, College college) {
        this.name = name;
        this.url = url;
        this.college = college;
    }

    /**
     * YU 뉴스인지 판단하는 도메인 로직
     *
     * @return : name이 영대소식이면 true
     */
    public boolean isYuNews() {
        return YU_NEWS_NAME.equals(this.name);
    }
}
