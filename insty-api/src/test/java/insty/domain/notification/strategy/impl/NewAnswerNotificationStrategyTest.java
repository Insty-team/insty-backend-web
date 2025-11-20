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
class NewAnswerNotificationStrategyTest {

    @InjectMocks
    private NewAnswerNotificationStrategy strategy;

    @Mock
    private NotificationUtils notificationUtils;

    private NotificationReq request;

    @BeforeEach
    void setUp() {
        request = NotificationReq.newAnswer(
                1L,
                100L,
                200L,
                "자바 스프링 질문입니다",
                "JPA는 Java Persistence API의 약자입니다",
                "김답변"
        );
    }

    @Test
    void 알림_타입_확인() {
        // When
        NotificationType type = strategy.getType();

        // Then
        assertEquals(NotificationType.NEW_COMMUNITY_ANSWER, type);
    }

    @Test
    void 인앱_알림_데이터_빌드_성공() {
        // Given
        String expectedUrl = "https://example.com/community/questions/100#answer-200";
        when(notificationUtils.truncateContent("자바 스프링 질문입니다", NotificationConstants.TITLE_MAX_LENGTH))
                .thenReturn("자바 스프링 질문입니다");
        when(notificationUtils.buildAnswerUrl(100L, 200L))
                .thenReturn(expectedUrl);

        // When
        NotificationData data = strategy.buildNotificationData(request);

        // Then
        assertNotNull(data);
        assertEquals("새로운 답변이 달렸습니다", data.title());
        assertTrue(data.message().contains("김답변"));
        assertTrue(data.message().contains("자바 스프링 질문입니다"));
        assertEquals(expectedUrl, data.redirectUrl());
    }

    @Test
    void 인앱_알림_데이터_빌드_긴_제목_자르기() {
        // Given
        String longTitle = "a".repeat(200);
        String truncatedTitle = "a".repeat(100) + "...";

        NotificationReq longTitleRequest = NotificationReq.newAnswer(
                1L,
                100L,
                200L,
                longTitle,
                "답변 내용",
                "김답변"
        );

        String expectedUrl = "https://example.com/community/questions/100#answer-200";
        when(notificationUtils.truncateContent(longTitle, NotificationConstants.TITLE_MAX_LENGTH))
                .thenReturn(truncatedTitle);
        when(notificationUtils.buildAnswerUrl(100L, 200L))
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
        String expectedAnswerUrl = "https://example.com/community/questions/100#answer-200";

        when(notificationUtils.buildQuestionUrl(100L))
                .thenReturn(expectedQuestionUrl);
        when(notificationUtils.buildAnswerUrl(100L, 200L))
                .thenReturn(expectedAnswerUrl);

        // When
        MailContent mailContent = strategy.buildMailContent(request, recipientEmail);

        // Then
        assertNotNull(mailContent);
        assertEquals(recipientEmail, mailContent.to());
        assertEquals(MailType.COMMUNITY_ANSWER, mailContent.mailType());

        Map<String, Object> variables = mailContent.variables();
        assertEquals(100L, variables.get("questionId"));
        assertEquals(200L, variables.get("answerId"));
        assertEquals("자바 스프링 질문입니다", variables.get("questionTitle"));
        assertEquals("JPA는 Java Persistence API의 약자입니다", variables.get("answerContent"));
        assertEquals("김답변", variables.get("answerAuthorNickname"));
        assertEquals(expectedQuestionUrl, variables.get("questionUrl"));
        assertEquals(expectedAnswerUrl, variables.get("answerUrl"));
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
