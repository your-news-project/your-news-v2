package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.repository.messageprocess.MessageProcessHourlyAggregation;
import kr.co.yournews.domain.notification.repository.messageprocess.MessageProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageProcessService {
    private final MessageProcessRepository messageProcessRepository;

    public List<MessageProcessHourlyAggregation> readHourlyAggregations(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return messageProcessRepository.findHourlyAggregations(startDateTime, endDateTime);
    }

    public int deleteByCreatedAtBefore(LocalDateTime dateTime) {
        return messageProcessRepository.deleteByCreatedAtBefore(dateTime);
    }
}
