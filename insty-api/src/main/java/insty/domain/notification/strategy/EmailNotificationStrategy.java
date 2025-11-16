package insty.domain.notification.strategy;

import insty.domain.notification.dto.NotificationRequest;
import insty.mail.MailContent;
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
     *
     * @param request 알림 요청 데이터
     * @return 이메일 전송 여부
     */
    default boolean shouldSendEmail(NotificationRequest request) {
        return true; // 기본적으로 모두 허용, 필요 시 오버라이드
    }

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
