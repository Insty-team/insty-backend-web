package insty.domain.notification.validation;

import insty.domain.user.implement.UserNotificationPreferenceValidator;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewQuestionNotificationValidator {

    private final UserNotificationPreferenceValidator userNotificationPreferenceValidator;

    public boolean validateUserNotification(User user) {
        return userNotificationPreferenceValidator.shouldReceiveNewQuestionEmail(user);
    }
}
