package kr.co.yournews.domain.user.repository;

import kr.co.yournews.domain.user.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUser_IdAndDeviceInfo(Long userId, String deviceInfo);
    List<FcmToken> findAllByUser_Id(Long userId);
    void deleteByToken(String token);
    void deleteAllByUser_Id(Long userId);
    void deleteByUser_IdAndDeviceInfo(Long userId, String deviceInfo);
}
