package insty.domain.notification.strategy;

import insty.mail.MailContent;
import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import insty.notification.NotificationType;

/**
 * 이메일 알림 전략 인터페이스
 * 이메일 전송에 대한 책임만 가짐
 */
public interface EmailNotificationStrategy {

    /**
     * 이 전략이 처리하는 알림 타입을 반환
     */
    NotificationType getType();

    /**
     * 이메일을 전송해야 하는지 검증
     *
     * @param request 알림 요청 데이터
     * @param preference 사용자 알림 설정
     * @return 이메일 전송 여부
     */
    boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference);

    /**
     * 이메일 컨텐츠를 빌드
     * MailContent를 반환하여 MailHelper가 전송할 수 있도록 함
     *
     * @param request 알림 요청 데이터
     * @param recipientEmail 수신자 이메일 주소
     * @return MailContent 객체
     */
    MailContent buildMailContent(NotificationRequest request, String recipientEmail);
}
