package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.entity.NoticeSummary;
import kr.co.yournews.domain.notification.repository.NoticeSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeSummaryService {
    private final NoticeSummaryRepository noticeSummaryRepository;

    public void saveAll(List<NoticeSummary> noticeSummaries) {
        noticeSummaryRepository.saveAllInBatch(noticeSummaries);
    }

    public Optional<NoticeSummary> readByUrlHash(String urlHash) {
        return noticeSummaryRepository.findByUrlHash(urlHash);
    }
}
