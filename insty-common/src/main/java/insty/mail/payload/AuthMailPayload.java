package insty.mail.payload;

import insty.mail.MailPayload;

/**
 * 회원가입 인증 메일 Payload
 */
public record AuthMailPayload(
        String recipient,
        String username,
        String verifyLink
) implements MailPayload {

    @Override
    public String getRecipient() {
        return recipient;
    }
}
