package insty.mail.event;

import insty.mail.MailPayload;
import insty.mail.MailType;

/**
 * 메일 전송 이벤트
 * 메일 타입과 타입별 Payload를 함께 전달
 */
public record MailSendEvent(
        MailType type,
        MailPayload payload
) {
}
