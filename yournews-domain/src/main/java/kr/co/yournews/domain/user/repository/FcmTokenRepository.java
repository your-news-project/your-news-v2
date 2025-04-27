package kr.co.yournews.domain.user.repository;

import kr.co.yournews.domain.user.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUser_IdAndDeviceInfo(Long userId, String deviceInfo);
    List<FcmToken> findAllByUser_Id(Long userId);
    List<FcmToken> findAllByUserIdIn(List<Long> userIds);
    void deleteByToken(String token);
    void deleteByUser_IdAndDeviceInfo(Long userId, String deviceInfo);

    @Modifying
    @Query("DELETE FROM fcm_token f WHERE f.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
