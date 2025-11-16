package insty.domain.notification.strategy;

import insty.notification.NotificationType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationStrategyRegistry {

    private final Map<NotificationType, InAppNotificationStrategy> inAppStrategies;
    private final Map<NotificationType, EmailNotificationStrategy> emailStrategies;

    public NotificationStrategyRegistry(
            List<InAppNotificationStrategy> inAppStrategyList,
            List<EmailNotificationStrategy> emailStrategyList
    ) {
        this.inAppStrategies = inAppStrategyList.stream()
                .collect(Collectors.toMap(
                        InAppNotificationStrategy::getType,
                        Function.identity()
                ));

        this.emailStrategies = emailStrategyList.stream()
                .collect(Collectors.toMap(
                        EmailNotificationStrategy::getType,
                        Function.identity()
                ));
    }

    /* 인앱 알림 전략 조회 */
    public InAppNotificationStrategy getInAppStrategy(NotificationType type) {
        return inAppStrategies.get(type);
    }

    /* 이메일 전략 조회 */
    public EmailNotificationStrategy getEmailStrategy(NotificationType type) {
        return emailStrategies.get(type);
    }

    /* 인앱 알림 전략 존재 여부 */
    public boolean hasInAppStrategy(NotificationType type) {
        return inAppStrategies.containsKey(type);
    }

    /* 이메일 전략 존재 여부 */
    public boolean hasEmailStrategy(NotificationType type) {
        return emailStrategies.containsKey(type);
    }
}
