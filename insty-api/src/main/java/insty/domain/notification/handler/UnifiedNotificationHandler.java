package insty.domain.notification.handler;

import insty.domain.notification.service.NotificationService;
import insty.domain.notification.strategy.EmailNotificationStrategy;
import insty.domain.notification.strategy.InAppNotificationStrategy;
import insty.domain.notification.strategy.NotificationData;
import insty.domain.notification.strategy.NotificationStrategyRegistry;
import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.mail.MailContent;
import insty.mail.MailHelper;
import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 통합 알림 핸들러
 * 모든 알림 타입을 처리하는 단일 핸들러
 *
 * 개선된 Template Method Pattern:
 * 1. 전략 조회 (InApp/Email 분리)
 * 2. 사용자 설정 조회
 * 3. 인앱 알림 처리 (검증 → 빌드 → 저장)
 * 4. 이메일 처리 (검증 → 빌드 → 전송)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedNotificationHandler {

    private final NotificationStrategyRegistry strategyRegistry;
    private final NotificationService notificationService;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final MailHelper mailHelper;

    /**
     * 알림 요청 처리
     * 트랜잭션 커밋 후 비동기로 실행
     *
     * @param request 알림 요청
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationRequest request) {
        try {
            InAppNotificationStrategy inAppStrategy = strategyRegistry.getInAppStrategy(request.type());
            EmailNotificationStrategy emailStrategy = strategyRegistry.getEmailStrategy(request.type());

            UserNotificationPreference preference = preferenceRepository
                    .findByUserId(request.receiverId())
                    .orElseGet(() -> {
                        log.warn("알림 설정 없음 - 기본 설정 사용, userId: {}", request.receiverId());
                        return createDefaultPreference();
                    });

            if (inAppStrategy != null && inAppStrategy.shouldSendInAppNotification(request, preference)) {
                NotificationData data = inAppStrategy.buildNotificationData(request);
                notificationService.saveNotification(request, data);
            }

            if (emailStrategy != null && emailStrategy.shouldSendEmail(request, preference)) {
                sendEmail(request, emailStrategy, preference);
            }
        } catch (Exception e) {
            // 예외를 던지지 않아 다른 알림 처리에 영향을 주지 않음
        }
    }



    /**
     * 이메일 전송 (비동기)
     */
    protected void sendEmail(NotificationRequest request,
                             EmailNotificationStrategy emailStrategy,
                             UserNotificationPreference preference) {
        try {
            String recipientEmail = getRecipientEmail(preference);
            MailContent mailContent = emailStrategy.buildMailContent(request, recipientEmail);

            mailHelper.send(mailContent);

            log.debug("이메일 전송 요청 완료 - type: {}, recipient: {}",
                    request.type(), recipientEmail);

        } catch (Exception e) {
            log.error("이메일 전송 요청 실패 - type: {}, receiverId: {}",
                    request.type(), request.receiverId(), e);
            // 이메일 전송 실패가 알림 저장에 영향을 주지 않도록 예외를 잡음
        }
    }

    /**
     * 수신자 이메일 주소 조회
     */
    private String getRecipientEmail(UserNotificationPreference preference) {
        return preference.getUser().getEmail();
    }

    /**
     * 기본 알림 설정 생성 (모든 알림 활성화)
     */
    private UserNotificationPreference createDefaultPreference() {
        // 기본 설정은 모든 알림이 활성화된 상태
        return UserNotificationPreference.builder()
                .userMentionNotificationEnabled(true)
                .newQuestionNotificationEnabled(true)
                .newAnswerNotificationEnabled(true)
                .answerAcceptedNotificationEnabled(true)
                .userMentionEmailEnabled(true)
                .newQuestionEmailEnabled(true)
                .newAnswerEmailEnabled(true)
                .answerAcceptedEmailEnabled(true)
                .build();
    }
}
