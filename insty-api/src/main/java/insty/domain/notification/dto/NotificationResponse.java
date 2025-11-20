package insty.domain.notification.dto;

import insty.model.notification.Notification;
import insty.model.notification.NotificationState;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * 알림 설정 변경 요청 DTO
 */
@Schema(description = "알림 조회 요청 결과")
public record NotificationResponse(
        Long id,

        @Schema(description = "알림 제목")
        String title,

        @Schema(description = "알림 상세 메시지")
        String message,

        @Schema(description = "알림 선택시 이동할 redirect-url")
        String redirectUrl,

        @Schema(description = "알림 조회 상태")
        boolean isRead,

        @Schema(description = "알림 생성일")
        Instant createdAt
) {
    public static NotificationResponse from(
            Notification notification
    ){
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRedirectUrl(),
                notification.getState().equals(NotificationState.READ),
                notification.getCreatedAt()
        );
    }
    
}
