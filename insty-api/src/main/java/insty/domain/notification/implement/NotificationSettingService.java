package insty.domain.notification.implement;

import org.springframework.stereotype.Service;

@Service
public class NotificationSettingService {

    public boolean isEmailNotificationEnabled(Long userId) {
        // TODO: User 도메인에서 알림 설정 확인 로직 구현 필요
        // 현재는 무조건 true 반환
        return true;
    }
}
