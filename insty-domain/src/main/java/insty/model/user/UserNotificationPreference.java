package insty.model.user;

import insty.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "web_service", name = "user_notification_preferences")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserNotificationPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private boolean userMentionNotificationEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean newQuestionNotificationEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean newAnswerNotificationEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean answerAcceptedNotificationEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean userMentionEmailEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean newQuestionEmailEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean newAnswerEmailEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean answerAcceptedEmailEnabled = true;

    public static UserNotificationPreference createDefault(User user) {
        return UserNotificationPreference.builder()
                .user(user)
                .build();
    }

    public void updateUserMentionSettings(boolean notificationEnabled, boolean emailEnabled) {
        this.userMentionNotificationEnabled = notificationEnabled;
        this.userMentionEmailEnabled = emailEnabled;
    }

    public void updateNewQuestionSettings(boolean notificationEnabled, boolean emailEnabled) {
        this.newQuestionNotificationEnabled = notificationEnabled;
        this.newQuestionEmailEnabled = emailEnabled;
    }

    public void updateNewAnswerSettings(boolean notificationEnabled, boolean emailEnabled) {
        this.newAnswerNotificationEnabled = notificationEnabled;
        this.newAnswerEmailEnabled = emailEnabled;
    }

    public void updateAnswerAcceptedSettings(boolean notificationEnabled, boolean emailEnabled) {
        this.answerAcceptedNotificationEnabled = notificationEnabled;
        this.answerAcceptedEmailEnabled = emailEnabled;
    }

    public boolean shouldReceiveUserMentionEmail() {
        return userMentionEmailEnabled && user.isEmailAgreed();
    }

    public boolean shouldReceiveNewQuestionEmail() {
        return newQuestionEmailEnabled && user.isEmailAgreed();
    }

    public boolean shouldReceiveNewAnswerEmail() {
        return newAnswerEmailEnabled && user.isEmailAgreed();
    }

    public boolean shouldReceiveAnswerAcceptedEmail() {
        return answerAcceptedEmailEnabled && user.isEmailAgreed();
    }
}