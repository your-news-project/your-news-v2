package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.entity.MessageProcessHourlySummary;
import kr.co.yournews.domain.notification.repository.messageprocesssummary.MessageProcessHourlySummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageProcessHourlySummaryService {
    private final MessageProcessHourlySummaryRepository messageProcessHourlySummaryRepository;

    public void saveAll(List<MessageProcessHourlySummary> summaries) {
        messageProcessHourlySummaryRepository.saveAll(summaries);
    }
}
