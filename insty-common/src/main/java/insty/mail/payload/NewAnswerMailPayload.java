package insty.mail.payload;

import insty.mail.MailPayload;

/**
 * 새 답변 알림 메일 Payload
 */
public record NewAnswerMailPayload(
        String recipient,
        String questionTitle,
        String answerContent,
        String answerAuthorNickname,
        String questionUrl
) implements MailPayload {

    @Override
    public String getRecipient() {
        return recipient;
    }
}
