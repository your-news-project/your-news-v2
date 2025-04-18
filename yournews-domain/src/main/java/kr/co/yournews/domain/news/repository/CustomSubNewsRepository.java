package kr.co.yournews.domain.news.repository;

import kr.co.yournews.domain.news.entity.SubNews;

import java.util.List;

public interface CustomSubNewsRepository {
    void saveAllInBatch(List<SubNews> subNewsList);
}
