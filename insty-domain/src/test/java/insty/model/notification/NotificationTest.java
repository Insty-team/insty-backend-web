package insty.model.notification;

import insty.error.NotificationErrorCode;
import insty.exception.CustomException;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
class NotificationTest {

    @Test
    void create_정상() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        NotificationType type = NotificationType.INFO;
        String title = "알림 제목";
        String message = "알림 메시지";
        String redirectUrl = "/redirect";

        // when
        Notification notification = Notification.create(user, type, title, message, redirectUrl);

        // then
        assertThat(notification).isNotNull();
        assertThat(notification.getId()).isNull();
        assertThat(notification.getUser()).isEqualTo(user);
        assertThat(notification.getType()).isEqualTo(type);
        assertThat(notification.getTitle()).isEqualTo(title);
        assertThat(notification.getMessage()).isEqualTo(message);
        assertThat(notification.getRedirectUrl()).isEqualTo(redirectUrl);
        assertThat(notification.getState()).isEqualTo(NotificationState.UNREAD);
    }

    @Test
    void create_에러_type이_null이다() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        NotificationType type = null;
        String title = "알림 제목";
        String message = "알림 메시지";
        String redirectUrl = "/redirect";

        // when & then
        assertThatThrownBy(() -> Notification.create(user, type, title, message, redirectUrl))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_CREATE_ERROR);
    }

    @Test
    void create_에러_title이_null이다() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        NotificationType type = NotificationType.INFO;
        String title = null;
        String message = "알림 메시지";
        String redirectUrl = "/redirect";

        // when & then
        assertThatThrownBy(() -> Notification.create(user, type, title, message, redirectUrl))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_CREATE_ERROR);
    }

    @Test
    void create_에러_title이_공백만_있다() {
        // given
        User user = UserFixtureBuilder.getUserWithId();
        NotificationType type = NotificationType.INFO;
        String title = "    ";
        String message = "알림 메시지";
        String redirectUrl = "/redirect";

        // when & then
        assertThatThrownBy(() -> Notification.create(user, type, title, message, redirectUrl))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_CREATE_ERROR);
    }
}
