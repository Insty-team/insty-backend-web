package insty.domain.notification.validation;

import insty.domain.user.service.UserNotificationPreferenceService;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewAnswerNotificationValidator {

    private final UserNotificationPreferenceService userNotificationPreferenceService;

    public boolean validateUserNotification(User user) {
        return userNotificationPreferenceService.shouldReceiveNewAnswerEmail(user);
    }
}
