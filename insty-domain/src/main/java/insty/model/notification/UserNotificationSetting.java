package insty.model.notification;

import insty.model.BaseEntity;
import insty.model.user.User;
import insty.notification.NotificationChannel;
import insty.notification.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    schema = "web_service",
    name = "user_notification_settings",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"user_id", "notification_type", "channel"}
    ),
    indexes = {
        @Index(name = "idx_user_notification_settings_user_id", columnList = "user_id"),
        @Index(name = "idx_user_notification_settings_type_channel", columnList = "notification_type, channel")
    }
)
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static UserNotificationSetting createDefault(User user, NotificationType type, NotificationChannel channel) {
        return UserNotificationSetting.builder()
                .user(user)
                .notificationType(type)
                .channel(channel)
                .enabled(true)
                .build();
    }

    public static UserNotificationSetting createDisabled(User user, NotificationType type, NotificationChannel channel) {
        return UserNotificationSetting.builder()
                .user(user)
                .notificationType(type)
                .channel(channel)
                .enabled(false)
                .build();
    }
}
