package insty.mail.payload;

import insty.mail.MailPayload;

/**
 * 답변 채택 알림 메일 Payload
 */
public record AnswerAcceptMailPayload(
        String recipient,
        String questionTitle,
        String answerContent,
        String answerAuthorName,
        String questionAuthorName,
        String questionUrl
) implements MailPayload {

    @Override
    public String getRecipient() {
        return recipient;
    }
}
