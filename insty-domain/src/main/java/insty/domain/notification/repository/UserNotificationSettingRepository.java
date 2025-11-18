package insty.domain.notification.repository;

import insty.model.notification.UserNotificationSetting;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {

    /// 특정 사용자의 모든 알림 설정 조회
    List<UserNotificationSetting> findByUserId(Long userId);

    /// 특정 사용자의 특정 타입, 채널 설정 조회
    Optional<UserNotificationSetting> findByUserIdAndNotificationTypeAndChannel(
            Long userId,
            NotificationType notificationType,
            NotificationChannel channel
    );

    /// 특정 사용자의 활성화된 설정만 조회
    @Query("SELECT s FROM UserNotificationSetting s WHERE s.user.id = :userId AND s.enabled = true")
    List<UserNotificationSetting> findEnabledSettingsByUserId(@Param("userId") Long userId);

    /// 사용자 설정 존재 여부 확인
    boolean existsByUserIdAndNotificationTypeAndChannel(
            Long userId,
            NotificationType notificationType,
            NotificationChannel channel
    );

    /// 특정 사용자의 설정 일괄 삭제
    void deleteByUserId(Long userId);
}
