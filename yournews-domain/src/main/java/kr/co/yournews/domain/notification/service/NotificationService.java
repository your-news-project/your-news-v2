package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAllInBatch(notifications);
    }

    public Optional<Notification> readById(Long id) {
        return notificationRepository.findById(id);
    }

    public Page<Notification> readAllByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findAllByUserId(userId, pageable);
    }

    public Page<Notification> readAllByUserIdAndIsRead(Long userId, boolean isRead, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndIsRead(userId, isRead, pageable);
    }

    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }
}
