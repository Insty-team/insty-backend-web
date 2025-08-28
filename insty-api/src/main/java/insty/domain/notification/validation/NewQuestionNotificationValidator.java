package insty.domain.notification.validation;

import insty.domain.user.service.UserNotificationPreferenceService;
import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewQuestionNotificationValidator {

    private final UserNotificationPreferenceService userNotificationPreferenceService;

    public boolean validateUserNotification(User user) {
        return userNotificationPreferenceService.shouldReceiveNewQuestionEmail(user);
    }
}
