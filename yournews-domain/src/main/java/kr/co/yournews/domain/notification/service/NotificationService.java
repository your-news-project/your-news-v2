package kr.co.yournews.domain.notification.service;

import kr.co.yournews.domain.notification.entity.Notification;
import kr.co.yournews.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public Optional<Notification> readByUserIdAndPublicId(Long userId, String publicId) {
        return notificationRepository.findByUserIdAndPublicId(userId, publicId);
    }

    public Page<Notification> readAllByUserIdAndIsRead(Long userId, boolean isRead, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndIsRead(userId, isRead, pageable);
    }

    public Page<Notification> readAllByUserIdAndNewsNameAndIsRead(Long userId, String newsName, boolean isRead, Pageable pageable) {
        return notificationRepository.findAllByUserIdAndNewsNameAndIsRead(userId, newsName, isRead, pageable);
    }

    public Page<Notification> readByUserIdAndNewsNameNotInAndIsRead(Long userId, List<String> newsName, boolean isRead, Pageable pageable) {
        return notificationRepository.findByUserIdAndNewsNameNotInAndIsRead(userId, newsName, isRead, pageable);
    }

    public Long readUnreadCountByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }

    public void deleteByDateTime(LocalDateTime dateTime) {
        notificationRepository.deleteByDateTimeBefore(dateTime);
    }
}
