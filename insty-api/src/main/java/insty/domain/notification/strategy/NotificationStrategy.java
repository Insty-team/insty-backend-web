package insty.domain.notification.strategy;

import insty.model.user.UserNotificationPreference;
import insty.notification.NotificationRequest;
import insty.notification.NotificationType;

import java.util.Map;

/**
 * 알림 전략 인터페이스
 * 각 알림 타입별로 이 인터페이스를 구현하여 알림 처리 로직을 정의
 *
 * Strategy Pattern을 사용하여 알림 타입별 처리를 분리
 */
public interface NotificationStrategy {

    /**
     * 이 전략이 처리하는 알림 타입을 반환
     * Registry에서 전략을 자동 등록할 때 사용
     */
    NotificationType getType();

    /**
     * 알림을 전송해야 하는지 검증
     * 사용자의 알림 설정을 확인하여 알림 전송 여부를 결정
     *
     * @param request 알림 요청 데이터
     * @param preference 사용자 알림 설정
     * @return 알림 전송 여부 (true: 전송, false: 전송 안함)
     */
    boolean shouldNotify(NotificationRequest request, UserNotificationPreference preference);

    /**
     * 이메일을 전송해야 하는지 검증
     * 사용자의 이메일 설정을 확인하여 이메일 전송 여부를 결정
     *
     * @param request 알림 요청 데이터
     * @param preference 사용자 알림 설정
     * @return 이메일 전송 여부 (true: 전송, false: 전송 안함)
     */
    boolean shouldSendEmail(NotificationRequest request, UserNotificationPreference preference);

    /**
     * 알림 데이터를 빌드
     * 알림 엔티티 저장에 필요한 제목, 메시지, URL을 생성
     *
     * @param request 알림 요청 데이터
     * @return 알림 데이터 (title, message, redirectUrl)
     */
    NotificationData buildNotification(NotificationRequest request);

    /**
     * 이메일 컨텍스트를 빌드 (선택적)
     * 이메일 템플릿 렌더링에 필요한 데이터를 생성
     * 기본 구현은 요청의 context를 그대로 반환
     *
     * @param request 알림 요청 데이터
     * @return 이메일 템플릿에 전달할 컨텍스트 맵
     */
    default Map<String, Object> buildEmailContext(NotificationRequest request) {
        return request.context();
    }

    /**
     * 이메일 템플릿 이름을 반환 (선택적)
     * 기본 구현은 NotificationType의 templateName을 사용
     *
     * @return 이메일 템플릿 파일 이름 (확장자 제외)
     */
    default String getEmailTemplate() {
        return getType().getTemplateName();
    }
}
