package insty.domain.notification.dto;

import insty.model.notification.Notification;
import insty.model.notification.NotificationState;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String redirectUrl,
        boolean isRead,
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
