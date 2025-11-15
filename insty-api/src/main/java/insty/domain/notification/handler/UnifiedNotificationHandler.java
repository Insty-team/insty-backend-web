package insty.domain.notification.handler;

import insty.domain.notification.repository.NotificationRepository;
import insty.domain.notification.strategy.NotificationData;
import insty.domain.notification.strategy.NotificationStrategy;
import insty.domain.notification.strategy.NotificationStrategyRegistry;
import insty.domain.user.repository.UserNotificationPreferenceRepository;
import insty.mail.MailService;
import insty.model.notification.Notification;
import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * 통합 알림 핸들러
 * 모든 알림 타입을 처리하는 단일 핸들러
 *
 * Template Method Pattern:
 * 1. 전략 조회
 * 2. 사용자 설정 조회
 * 3. 알림 전송 여부 검증
 * 4. 알림 데이터 빌드 및 저장
 * 5. 이메일 전송 (비동기)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedNotificationHandler {

    private final NotificationStrategyRegistry strategyRegistry;
    private final NotificationRepository notificationRepository;
    private final UserNotificationPreferenceRepository preferenceRepository;
    private final MailService mailService;

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
            log.debug("알림 처리 시작 - type: {}, receiverId: {}", request.type(), request.receiverId());

            // 1. 전략 조회
            NotificationStrategy strategy = strategyRegistry.getStrategy(request.type());

            // 2. 사용자 설정 조회
            UserNotificationPreference preference = preferenceRepository
                    .findByUserId(request.receiverId())
                    .orElseGet(() -> {
                        log.warn("알림 설정 없음 - 기본 설정 사용, userId: {}", request.receiverId());
                        return createDefaultPreference();
                    });

            // 3. 알림 전송 여부 검증
            if (!strategy.shouldNotify(request, preference)) {
                log.debug("알림 전송 거부 - type: {}, receiverId: {}", request.type(), request.receiverId());
                return;
            }

            // 4. 알림 데이터 빌드 및 저장
            NotificationData data = strategy.buildNotification(request);
            saveNotification(request, data);

            // 5. 이메일 전송 (비동기)
            if (strategy.shouldSendEmail(request, preference)) {
                sendEmail(request, strategy, preference);
            }

            log.info("알림 처리 완료 - type: {}, receiverId: {}", request.type(), request.receiverId());

        } catch (Exception e) {
            log.error("알림 처리 실패 - type: {}, receiverId: {}",
                    request.type(), request.receiverId(), e);
            // 예외를 던지지 않아 다른 알림 처리에 영향을 주지 않음
        }
    }

    /**
     * 알림 저장
     */
    @Transactional
    protected void saveNotification(NotificationRequest request, NotificationData data) {
        Notification notification = Notification.create(
                request.receiverId(),
                request.type(),
                data.title(),
                data.message(),
                data.redirectUrl()
        );

        notificationRepository.save(notification);
        log.debug("알림 저장 완료 - type: {}, userId: {}, title: {}",
                request.type(), request.receiverId(), data.title());
    }

    /**
     * 이메일 전송 (비동기)
     */
    protected void sendEmail(NotificationRequest request,
                             NotificationStrategy strategy,
                             UserNotificationPreference preference) {
        try {
            String recipientEmail = getRecipientEmail(preference);
            String subject = request.type().getEmailSubject();
            String templateName = strategy.getEmailTemplate();
            Map<String, Object> context = strategy.buildEmailContext(request);

            mailService.sendTemplatedMail(recipientEmail, subject, templateName, context);

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
