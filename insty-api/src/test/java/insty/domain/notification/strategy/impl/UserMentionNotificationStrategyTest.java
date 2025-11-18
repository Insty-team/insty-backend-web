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
class UserMentionNotificationStrategyTest {

    @InjectMocks
    private UserMentionNotificationStrategy strategy;

    @Mock
    private NotificationUtils notificationUtils;

    private NotificationRequest request;

    @BeforeEach
    void setUp() {
        request = NotificationRequest.userMentioned(
                1L,
                300L,
                "홍길동",
                "@김철수님 안녕하세요. 질문이 있습니다.",
                "QUESTION",
                100L
        );
    }

    @Test
    void 알림_타입_확인() {
        // When
        NotificationType type = strategy.getType();

        // Then
        assertEquals(NotificationType.USER_MENTIONED, type);
    }

    @Test
    void 인앱_알림_데이터_빌드_성공() {
        // Given
        String expectedUrl = "https://example.com/community/questions/100";
        when(notificationUtils.truncateContent(
                "@김철수님 안녕하세요. 질문이 있습니다.",
                NotificationConstants.CONTENT_MAX_LENGTH))
                .thenReturn("@김철수님 안녕하세요. 질문이 있습니다.");
        when(notificationUtils.buildMentionUrl("QUESTION", 100L))
                .thenReturn(expectedUrl);

        // When
        NotificationData data = strategy.buildNotificationData(request);

        // Then
        assertNotNull(data);
        assertEquals("누군가 당신을 언급했습니다", data.title());
        assertTrue(data.message().contains("홍길동"));
        assertTrue(data.message().contains("@김철수님 안녕하세요. 질문이 있습니다."));
        assertEquals(expectedUrl, data.redirectUrl());
    }

    @Test
    void 인앱_알림_데이터_빌드_긴_내용_자르기() {
        // Given
        String longContent = "a".repeat(500);
        String truncatedContent = "a".repeat(200) + "...";

        NotificationRequest longContentRequest = NotificationRequest.userMentioned(
                1L,
                300L,
                "홍길동",
                longContent,
                "QUESTION",
                100L
        );

        String expectedUrl = "https://example.com/community/questions/100";
        when(notificationUtils.truncateContent(longContent, NotificationConstants.CONTENT_MAX_LENGTH))
                .thenReturn(truncatedContent);
        when(notificationUtils.buildMentionUrl("QUESTION", 100L))
                .thenReturn(expectedUrl);

        // When
        NotificationData data = strategy.buildNotificationData(longContentRequest);

        // Then
        assertNotNull(data);
        assertTrue(data.message().contains(truncatedContent));
    }

    @Test
    void 이메일_컨텐츠_빌드_질문_타입() {
        // Given
        String recipientEmail = "test@example.com";
        String expectedMentionUrl = "https://example.com/community/questions/100";

        when(notificationUtils.buildMentionUrl("QUESTION", 100L))
                .thenReturn(expectedMentionUrl);

        // When
        MailContent mailContent = strategy.buildMailContent(request, recipientEmail);

        // Then
        assertNotNull(mailContent);
        assertEquals(recipientEmail, mailContent.to());
        assertEquals(MailType.MENTION, mailContent.mailType());

        Map<String, Object> variables = mailContent.variables();
        assertEquals(300L, variables.get("mentionId"));
        assertEquals("홍길동", variables.get("mentionerNickname"));
        assertEquals("@김철수님 안녕하세요. 질문이 있습니다.", variables.get("content"));
        assertEquals("QUESTION", variables.get("contentType"));
        assertEquals(100L, variables.get("relatedId"));
        assertEquals(expectedMentionUrl, variables.get("mentionUrl"));
    }

    @Test
    void 이메일_컨텐츠_빌드_답변_타입() {
        // Given
        NotificationRequest answerMentionRequest = NotificationRequest.userMentioned(
                1L,
                300L,
                "홍길동",
                "@김철수님 답변 드립니다.",
                "ANSWER",
                100L
        );

        String recipientEmail = "test@example.com";
        String expectedMentionUrl = "https://example.com/community/questions/100";

        when(notificationUtils.buildMentionUrl("ANSWER", 100L))
                .thenReturn(expectedMentionUrl);

        // When
        MailContent mailContent = strategy.buildMailContent(answerMentionRequest, recipientEmail);

        // Then
        assertNotNull(mailContent);
        assertEquals(recipientEmail, mailContent.to());
        assertEquals(MailType.MENTION, mailContent.mailType());

        Map<String, Object> variables = mailContent.variables();
        assertEquals("ANSWER", variables.get("contentType"));
        assertEquals(expectedMentionUrl, variables.get("mentionUrl"));
    }

    @Test
    void 이메일_컨텐츠_빌드_댓글_타입() {
        // Given
        NotificationRequest commentMentionRequest = NotificationRequest.userMentioned(
                1L,
                300L,
                "홍길동",
                "@김철수님 댓글 남깁니다.",
                "COMMENT",
                100L
        );

        String recipientEmail = "test@example.com";
        String expectedMentionUrl = "https://example.com/community/questions/100";

        when(notificationUtils.buildMentionUrl("COMMENT", 100L))
                .thenReturn(expectedMentionUrl);

        // When
        MailContent mailContent = strategy.buildMailContent(commentMentionRequest, recipientEmail);

        // Then
        assertNotNull(mailContent);
        Map<String, Object> variables = mailContent.variables();
        assertEquals("COMMENT", variables.get("contentType"));
        assertEquals(expectedMentionUrl, variables.get("mentionUrl"));
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
