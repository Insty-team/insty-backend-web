package insty.mail.payload;

import insty.mail.MailPayload;

/**
 * 사용자 멘션 알림 메일 Payload
 */
public record MentionMailPayload(
        String recipient,
        String questionTitle,
        String mentionerName,
        String questionUrl
) implements MailPayload {

    @Override
    public String getRecipient() {
        return recipient;
    }
}
