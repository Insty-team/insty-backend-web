package insty.domain.notification.strategy;

import insty.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알림 전략 레지스트리 (개선됨)
 * Spring DI를 통해 InApp/Email 전략을 자동으로 등록
 *
 * 관심사 분리된 전략 조회 지원:
 * - getInAppStrategy(): 인앱 알림 전용
 * - getEmailStrategy(): 이메일 전용
 */
@Slf4j
@Component
public class NotificationStrategyRegistry {

    private final Map<NotificationType, InAppNotificationStrategy> inAppStrategies;
    private final Map<NotificationType, EmailNotificationStrategy> emailStrategies;

    /**
     * 생성자
     * Spring이 자동으로 모든 전략 빈을 주입
     */
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

        log.info("NotificationStrategyRegistry 초기화 완료 - InApp: {}, Email: {}",
                inAppStrategies.size(), emailStrategies.size());
    }

    /**
     * 인앱 알림 전략 조회
     */
    public InAppNotificationStrategy getInAppStrategy(NotificationType type) {
        return inAppStrategies.get(type);
    }

    /**
     * 이메일 전략 조회
     */
    public EmailNotificationStrategy getEmailStrategy(NotificationType type) {
        return emailStrategies.get(type);
    }

    /**
     * 인앱 알림 전략 존재 여부
     */
    public boolean hasInAppStrategy(NotificationType type) {
        return inAppStrategies.containsKey(type);
    }

    /**
     * 이메일 전략 존재 여부
     */
    public boolean hasEmailStrategy(NotificationType type) {
        return emailStrategies.containsKey(type);
    }
}
