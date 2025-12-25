package insty.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.ai.adapter.AiRequester;
import insty.cloudfront.adapter.CloudFrontSigner;
import insty.domain.notification.dto.event.NotificationData;
import insty.domain.notification.dto.event.NotificationReq;
import insty.domain.notification.dto.response.NotificationRes;
import insty.domain.notification.repository.NotificationRepository;
import insty.domain.user.repository.UserRepository;
import insty.error.NotificationErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.notification.Notification;
import insty.model.notification.NotificationState;
import insty.model.user.User;
import insty.notification.NotificationType;
import insty.s3.adapter.S3FileManager;
import insty.s3.adapter.S3UrlIssuer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Tag("integration")
class NotificationServiceIntTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private S3UrlIssuer s3UrlIssuer;

    @MockitoBean
    private S3FileManager s3FileManager;

    @MockitoBean
    private CloudFrontSigner cloudFrontSigner;

    @MockitoBean
    private AiRequester aiRequester;

    @MockitoBean
    private AppProperties appProperties;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create(
                "test@example.com",
                "password123",
                "테스트유저"
        );
        userRepository.save(testUser);
    }

    @Test
    void 사용자_알림_조회_성공() {
        // Given
        Notification notification1 = Notification.create(
                testUser.getId(),
                NotificationType.NEW_COURSE_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                "/questions/1"
        );

        Notification notification2 = Notification.create(
                testUser.getId(),
                NotificationType.NEW_COURSE_ANSWER,
                "새로운 답변",
                "답변이 등록되었습니다",
                "/questions/1#answer-1"
        );

        notificationRepository.save(notification1);
        notificationRepository.save(notification2);

        // When
        List<NotificationRes> result = notificationService.getUserNotifications(testUser.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationRes::title)
                .containsExactlyInAnyOrder("새로운 질문", "새로운 답변");
    }

    @Test
    void 사용자_알림_조회_빈_리스트() {
        // When
        List<NotificationRes> result = notificationService.getUserNotifications(testUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void 사용자_알림_조회_실패_사용자_없음() {
        // Given
        Long nonExistentUserId = 999L;

        // When & Then
        assertThatThrownBy(() -> notificationService.getUserNotifications(nonExistentUserId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 알림_저장_성공() {
        // Given
        NotificationReq request = NotificationReq.newCourseQuestion(
                testUser.getId(),
                1L,
                "자바 스프링 질문입니다",
                "스프링 부트에서 JPA 사용법을 알고 싶습니다",
                "홍길동",
                "스프링 부트 완전정복"
        );

        NotificationData data = new NotificationData(
                "새로운 질문",
                "질문이 등록되었습니다",
                "/questions/1"
        );

        // When
        notificationService.saveNotification(request, data);

        // Then
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).hasSize(1);

        Notification savedNotification = notifications.get(0);
        assertThat(savedNotification.getUserId()).isEqualTo(testUser.getId());
        assertThat(savedNotification.getType()).isEqualTo(NotificationType.NEW_COURSE_QUESTION);
        assertThat(savedNotification.getTitle()).isEqualTo("새로운 질문");
        assertThat(savedNotification.getMessage()).isEqualTo("질문이 등록되었습니다");
        assertThat(savedNotification.getRedirectUrl()).isEqualTo("/questions/1");
        assertThat(savedNotification.getState()).isEqualTo(NotificationState.UNREAD);
    }

    @Test
    void 알림_읽음_처리_및_리다이렉트_성공() {
        // Given
        String redirectUrl = "/questions/1";
        Notification notification = Notification.create(
                testUser.getId(),
                NotificationType.NEW_COURSE_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                redirectUrl
        );
        notificationRepository.save(notification);

        // When
        String result = notificationService.markAsReadAndRedirect(notification.getId(), testUser.getId());

        // Then
        assertThat(result).isEqualTo(redirectUrl);

        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.getState()).isEqualTo(NotificationState.READ);
    }

    @Test
    void 알림_읽음_처리_실패_알림_없음() {
        // Given
        Long nonExistentNotificationId = 999L;

        // When & Then
        assertThatThrownBy(() -> notificationService.markAsReadAndRedirect(
                nonExistentNotificationId, testUser.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }

    @Test
    void 알림_읽음_처리_실패_권한_없음() {
        // Given
        User anotherUser = User.create(
                "another@example.com",
                "password123",
                "다른유저"
        );
        userRepository.save(anotherUser);

        Notification notification = Notification.create(
                anotherUser.getId(),
                NotificationType.NEW_COURSE_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                "/questions/1"
        );
        notificationRepository.save(notification);

        // When & Then
        assertThatThrownBy(() -> notificationService.markAsReadAndRedirect(
                notification.getId(), testUser.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOTIFICATION_PREFERENCE_NOT_FOUND);
    }

    @Test
    void 알림_읽음_처리_이미_읽은_알림() {
        // Given
        String redirectUrl = "/questions/1";
        Notification notification = Notification.create(
                testUser.getId(),
                NotificationType.NEW_COURSE_QUESTION,
                "새로운 질문",
                "질문이 등록되었습니다",
                redirectUrl
        );
        notification.markAsRead();
        notificationRepository.save(notification);

        // When
        String result = notificationService.markAsReadAndRedirect(notification.getId(), testUser.getId());

        // Then
        assertThat(result).isEqualTo(redirectUrl);

        Notification updatedNotification = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updatedNotification.getState()).isEqualTo(NotificationState.READ);
    }

    @Test
    void 여러_알림_타입_저장_및_조회() {
        // Given
        NotificationReq request1 = NotificationReq.newCourseQuestion(
                testUser.getId(), 1L, "질문 제목", "질문 내용", "작성자1", "강의명"
        );
        NotificationData data1 = new NotificationData("새로운 질문", "질문이 등록되었습니다", "/questions/1");

        NotificationReq request2 = NotificationReq.newAnswer(
                testUser.getId(), 1L, 2L, "질문 제목", "답변 내용", "작성자2"
        );
        NotificationData data2 = new NotificationData("새로운 답변", "답변이 등록되었습니다", "/questions/1#answer-2");

        NotificationReq request3 = NotificationReq.answerAccepted(
                testUser.getId(), 1L, 2L, "질문 제목", "답변 내용"
        );
        NotificationData data3 = new NotificationData("답변 채택", "답변이 채택되었습니다", "/questions/1#answer-2");

        // When
        notificationService.saveNotification(request1, data1);
        notificationService.saveNotification(request2, data2);
        notificationService.saveNotification(request3, data3);

        // Then
        List<NotificationRes> notifications = notificationService.getUserNotifications(testUser.getId());
        assertThat(notifications).hasSize(3);
        assertThat(notifications).extracting(NotificationRes::title)
                .containsExactlyInAnyOrder("새로운 질문", "새로운 답변", "답변 채택");

        // 데이터베이스에 직접 저장된 알림 타입 검증
        List<Notification> savedNotifications = notificationRepository.findAll();
        assertThat(savedNotifications).extracting(Notification::getType)
                .containsExactlyInAnyOrder(
                        NotificationType.NEW_COURSE_QUESTION,
                        NotificationType.NEW_COURSE_ANSWER,
                        NotificationType.COURSE_ANSWER_ACCEPT
                );
    }
}
