package insty.domain.notification.handler;

import insty.domain.notification.dto.NotificationData;
import insty.domain.notification.dto.NotificationRequest;
import insty.domain.notification.service.NotificationPreferenceService;
import insty.domain.notification.service.NotificationService;
import insty.domain.notification.strategy.EmailNotificationStrategy;
import insty.domain.notification.strategy.InAppNotificationStrategy;
import insty.domain.notification.strategy.NotificationStrategyRegistry;
import insty.domain.user.repository.UserRepository;
import insty.mail.MailContent;
import insty.mail.MailHelper;
import insty.model.user.User;
import insty.notification.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 모든 알림 타입을 처리하는 핸들러
 *
 * Template Method Pattern:
 * 1. 전략 조회 (InApp/Email 분리)
 * 2. 사용자 설정 조회 (NotificationPreferenceService - Key-Value 기반)
 * 3. 인앱 알림 처리 (설정 확인 → 전략 검증 → 빌드 → 저장)
 * 4. 이메일 처리 (설정 확인 → 전략 검증 → 빌드 → 전송)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedNotificationHandler {

    private final NotificationStrategyRegistry strategyRegistry;
    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final UserRepository userRepository;
    private final MailHelper mailHelper;

    /**
     * 알림 요청 처리 (트랜잭션 커밋 후 비동기 실행)
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationRequest request) {
        try {
            InAppNotificationStrategy inAppStrategy = strategyRegistry.getInAppStrategy(request.type());
            EmailNotificationStrategy emailStrategy = strategyRegistry.getEmailStrategy(request.type());

            User user = userRepository.findById(request.receiverId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.receiverId()));

            // 인앱 알림 처리
            if (inAppStrategy != null) {
                processInAppNotification(request, inAppStrategy);
            }

            // 이메일 처리
            if (emailStrategy != null) {
                processEmail(request, emailStrategy, user);
            }

        } catch (Exception e) {
            log.error("알림 처리 실패 - type: {}, receiverId: {}", request.type(), request.receiverId(), e);
        }
    }

    /* 인앱 알림 처리 */
    private void processInAppNotification(NotificationRequest request, InAppNotificationStrategy strategy) {
        try {
            // 1. 사용자 설정 확인
            boolean enabled = preferenceService.isNotificationEnabled(
                    request.receiverId(),
                    request.type(),
                    NotificationChannel.IN_APP
            );

            if (!enabled) {
                return;
            }

            // 2. 전략의 비즈니스 로직 검증
            if (!strategy.shouldSendInAppNotification(request)) {
                return;
            }

            // 3. 알림 데이터 빌드 및 저장
            NotificationData data = strategy.buildNotificationData(request);
            notificationService.saveNotification(request, data);

        } catch (Exception e) {
            log.error("인앱 알림 처리 실패 - type: {}, receiverId: {}", request.type(), request.receiverId(), e);
        }
    }

    /**
     * 이메일 처리
     */
    private void processEmail(NotificationRequest request, EmailNotificationStrategy strategy, User user) {
        try {
            // 1. 이메일 수신 설정 확인 (사용자 설정 + 이메일 동의 여부)
            boolean enabled = preferenceService.isEmailEnabled(user, request.type());

            if (!enabled) {
                return;
            }

            // 2. 전략의 비즈니스 로직 검증
            if (!strategy.shouldSendEmail(request)) {
                return;
            }

            // 3. 이메일 컨텐츠 빌드 및 전송
            MailContent mailContent = strategy.buildMailContent(request, user.getEmail());
            mailHelper.send(mailContent);

        } catch (Exception e) {
            log.error("이메일 전송 실패 - type: {}, receiverId: {}", request.type(), request.receiverId(), e);
        }
    }
}
