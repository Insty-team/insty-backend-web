package insty.domain.notification.strategy.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import insty.constants.NotificationConstants;
import insty.domain.notification.dto.NotificationData;
import insty.domain.notification.dto.NotificationRequest;
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
class AnswerAcceptNotificationStrategyTest {

    @InjectMocks
    private AnswerAcceptNotificationStrategy strategy;

    @Mock
    private NotificationUtils notificationUtils;

    private NotificationRequest request;

    @BeforeEach
    void setUp() {
        request = NotificationRequest.answerAccepted(
                1L,
                100L,
                200L,
                "자바 스프링 질문입니다",
                "JPA는 Java Persistence API의 약자입니다"
        );
    }

    @Test
    void 알림_타입_확인() {
        // When
        NotificationType type = strategy.getType();

        // Then
        assertEquals(NotificationType.COMMUNITY_ANSWER_ACCEPT, type);
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
        assertEquals("답변이 채택되었습니다", data.title());
        assertTrue(data.message().contains("자바 스프링 질문입니다"));
        assertTrue(data.message().contains("작성한 답변이 채택되었습니다"));
        assertEquals(expectedUrl, data.redirectUrl());
    }

    @Test
    void 인앱_알림_데이터_빌드_긴_제목_자르기() {
        // Given
        String longTitle = "a".repeat(200);
        String truncatedTitle = "a".repeat(100) + "...";

        NotificationRequest longTitleRequest = NotificationRequest.answerAccepted(
                1L,
                100L,
                200L,
                longTitle,
                "답변 내용"
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
        assertEquals(MailType.COMMUNITY_ANSWER_ACCEPT, mailContent.mailType());

        Map<String, Object> variables = mailContent.variables();
        assertEquals(100L, variables.get("questionId"));
        assertEquals(200L, variables.get("answerId"));
        assertEquals("자바 스프링 질문입니다", variables.get("questionTitle"));
        assertEquals("JPA는 Java Persistence API의 약자입니다", variables.get("answerContent"));
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
