package insty.domain.notification.validation;

import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewQuestionNotificationValidator {

    public boolean validateUserNotification(User user) {
        // todo : 실재 체크 알림 검증 로직
        return true;
    }
}
