package insty.domain.notification.validation;

import insty.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewAnswerNotificationValidator {

    public boolean validateUserNotification(User user) {
        return true;
    }
}
