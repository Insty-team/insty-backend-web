package insty.mail.payload;

import insty.mail.MailPayload;

/**
 * 커뮤니티 새 질문 알림 메일 Payload
 */
public record CommunityQuestionMailPayload(
        String recipient,
        String questionTitle,
        String questionContent,
        String questionAuthorName,
        String courseName,
        String questionUrl
) implements MailPayload {

    @Override
    public String getRecipient() {
        return recipient;
    }
}
