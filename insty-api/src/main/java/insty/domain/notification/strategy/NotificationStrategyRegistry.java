package insty.domain.notification.strategy;

import insty.notification.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알림 전략 레지스트리
 * Spring의 의존성 주입을 통해 모든 NotificationStrategy 구현체를 자동으로 등록
 *
 * Registry Pattern을 사용하여 전략 선택을 자동화
 */
@Slf4j
@Component
public class NotificationStrategyRegistry {

    private final Map<NotificationType, NotificationStrategy> strategies;

    /**
     * 생성자
     * Spring이 자동으로 모든 NotificationStrategy 빈을 주입
     *
     * @param strategyList Spring 컨텍스트에 등록된 모든 NotificationStrategy 구현체
     */
    public NotificationStrategyRegistry(List<NotificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        NotificationStrategy::getType,
                        Function.identity()
                ));

        log.info("NotificationStrategyRegistry 초기화 완료 - 등록된 전략 수: {}", strategies.size());
        strategies.forEach((type, strategy) ->
                log.debug("등록된 전략: {} -> {}", type, strategy.getClass().getSimpleName())
        );
    }

    /**
     * 알림 타입에 해당하는 전략을 반환
     *
     * @param type 알림 타입
     * @return 해당 타입을 처리하는 전략
     * @throws IllegalStateException 전략이 등록되지 않은 경우
     */
    public NotificationStrategy getStrategy(NotificationType type) {
        NotificationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            log.error("등록되지 않은 알림 타입: {}", type);
            throw new IllegalStateException("No strategy found for notification type: " + type);
        }
        return strategy;
    }

    /**
     * 특정 알림 타입에 대한 전략이 등록되어 있는지 확인
     *
     * @param type 알림 타입
     * @return 등록 여부
     */
    public boolean hasStrategy(NotificationType type) {
        return strategies.containsKey(type);
    }
}
