package insty.domain.notification.validation;

import insty.domain.user.implement.UserNotificationPreferenceValidator;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewAnswerNotificationValidator {

    private final UserNotificationPreferenceValidator userNotificationPreferenceValidator;

    public boolean validateUserNotification(User user) {
        return userNotificationPreferenceValidator.shouldReceiveNewAnswerEmail(user);
    }
}
