package kr.co.yournews.domain.user.service;

import kr.co.yournews.domain.user.entity.FcmToken;
import kr.co.yournews.domain.user.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {
    private final FcmTokenRepository fcmTokenRepository;

    public void save(FcmToken fcmToken) {
        fcmTokenRepository.save(fcmToken);
    }

    public List<FcmToken> readAllByUserId(Long userId) {
        return fcmTokenRepository.findAllByUser_Id(userId);
    }

    public List<FcmToken> readAllByUserIds(List<Long> userIds) {
        return fcmTokenRepository.findAllByUserIdIn(userIds);
    }

    public Optional<FcmToken> readByUserIdAndDeviceInfo(Long userId, String deviceInfo) {
        return fcmTokenRepository.findByUser_IdAndDeviceInfo(userId, deviceInfo);
    }

    public void deleteByToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    public void deleteAllByUserId(Long userId) {
        fcmTokenRepository.deleteAllByUserId(userId);
    }

    public void deleteByUserIdAndDeviceInfo(Long userId, String deviceInfo) {
        fcmTokenRepository.deleteByUser_IdAndDeviceInfo(userId, deviceInfo);
    }
}
