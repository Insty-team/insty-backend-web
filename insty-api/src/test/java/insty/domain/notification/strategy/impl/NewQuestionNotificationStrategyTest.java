package insty.domain.notification.strategy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import insty.constants.NotificationConstants;
import insty.domain.notification.dto.event.NotificationData;
import insty.domain.notification.dto.event.NotificationReq;
import insty.domain.notification.util.NotificationUtils;
import insty.mail.MailContent;
import insty.mail.MailType;
import insty.notification.NotificationType;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewQuestionNotificationStrategyTest {

    @InjectMocks
    private NewQuestionNotificationStrategy strategy;

    @Mock
    private NotificationUtils notificationUtils;

    private NotificationReq request;

    @BeforeEach
    void setUp() {
        request = NotificationReq.newCommunityQuestion(
                1L,
                100L,
                "자바 스프링 질문입니다",
                "스프링 부트에서 JPA 사용법을 알고 싶습니다",
                "홍길동",
                "스프링 부트 완전정복"
        );
    }

    @Test
    void 알림_타입_확인() {
        // When
        NotificationType type = strategy.getType();

        // Then
        assertEquals(NotificationType.NEW_COURSE_QUESTION, type);
    }

    @Test
    void 인앱_알림_데이터_빌드_성공() {
        // Given
        String expectedUrl = "https://example.com/community/questions/100";
        when(notificationUtils.truncateContent("자바 스프링 질문입니다", NotificationConstants.TITLE_MAX_LENGTH))
                .thenReturn("자바 스프링 질문입니다");
        when(notificationUtils.buildQuestionUrl(100L))
                .thenReturn(expectedUrl);

        // When
        NotificationData data = strategy.buildNotificationData(request);

        // Then
        assertNotNull(data);
        assertEquals("새로운 질문이 등록되었습니다", data.title());
        assertTrue(data.message().contains("홍길동"));
        assertTrue(data.message().contains("스프링 부트 완전정복"));
        assertTrue(data.message().contains("자바 스프링 질문입니다"));
        assertEquals(expectedUrl, data.redirectUrl());
    }

    @Test
    void 인앱_알림_데이터_빌드_긴_제목_자르기() {
        // Given
        String longTitle = "a".repeat(200);
        String truncatedTitle = "a".repeat(100) + "...";

        NotificationReq longTitleRequest = NotificationReq.newCommunityQuestion(
                1L,
                100L,
                longTitle,
                "질문 내용",
                "홍길동",
                "스프링 부트 완전정복"
        );

        String expectedUrl = "https://example.com/community/questions/100";
        when(notificationUtils.truncateContent(longTitle, NotificationConstants.TITLE_MAX_LENGTH))
                .thenReturn(truncatedTitle);
        when(notificationUtils.buildQuestionUrl(100L))
                .thenReturn(expectedUrl);

        // When
        NotificationData data = strategy.buildNotificationData(longTitleRequest);

        // Then
        assertNotNull(data);
        assertTrue(data.message().contains(truncatedTitle));
    }

    @Test
    void 이메일_컨텐츠_빌드_성공() {
        // Given
        String recipientEmail = "test@example.com";
        String expectedQuestionUrl = "https://example.com/community/questions/100";

        when(notificationUtils.buildQuestionUrl(100L))
                .thenReturn(expectedQuestionUrl);

        // When
        MailContent mailContent = strategy.buildMailContent(request, recipientEmail);

        // Then
        assertNotNull(mailContent);
        assertEquals(recipientEmail, mailContent.to());
        assertEquals(MailType.COURSE_QUESTION, mailContent.mailType());

        Map<String, Object> variables = mailContent.variables();
        assertEquals(100L, variables.get("questionId"));
        assertEquals("자바 스프링 질문입니다", variables.get("questionTitle"));
        assertEquals("스프링 부트에서 JPA 사용법을 알고 싶습니다", variables.get("questionContent"));
        assertEquals("홍길동", variables.get("questionAuthorName"));
        assertEquals("스프링 부트 완전정복", variables.get("courseName"));
        assertEquals(expectedQuestionUrl, variables.get("questionUrl"));
    }

    @Test
    void 인앱_알림_전송_여부_기본값_true() {
        // When
        boolean shouldSend = strategy.shouldSendInAppNotification(request);

        // Then
        assertTrue(shouldSend);
    }

    @Test
    void 이메일_전송_여부_기본값_true() {
        // When
        boolean shouldSend = strategy.shouldSendEmail(request);

        // Then
        assertTrue(shouldSend);
    }
}
