package insty.model.notification;

import insty.error.NotificationErrorCode;
import insty.exception.CustomException;
import insty.model.BaseEntity;
import insty.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String redirectUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationState state;

    public static Notification create(User user, NotificationType type, String title, String message,
                                      String redirectUrl) {
        validateCreate(user, type, title, message, redirectUrl);
        return Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .redirectUrl(redirectUrl)
                .state(NotificationState.UNREAD)
                .build();
    }

    private static void validateCreate(User user, NotificationType type, String title, String message,
                                       String redirectUrl) {
        if (type == null) {
            log.error("생성 오류 - notification : 알림 타입은 필수입니다");
            throw new CustomException(NotificationErrorCode.NOTIFICATION_CREATE_ERROR);
        }
        if (title == null || title.isBlank()) {
            log.error("생성 오류 - notification : 알림 제목은 비어 있을 수 없습니다");
            throw new CustomException(NotificationErrorCode.NOTIFICATION_CREATE_ERROR);
        }
    }

    public void markAsRead() {
        if (this.state == NotificationState.READ) {
            return;
        }
        this.state = NotificationState.READ;
    }

    public void markAsUnread() {
        if (this.state == NotificationState.UNREAD) {
            return;
        }
        this.state = NotificationState.UNREAD;
    }

    public void markAsDeleted() {
        this.state = NotificationState.DELETED;
    }
}
