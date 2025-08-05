package insty.domain.auth.implement.emailverification;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;

public record MailAuthenticateContent(
    String to,
    MailType mailType,
    String code
) implements MailContent {

    public static MailAuthenticateContent of(String to, String code) {
        return new MailAuthenticateContent(to, MailType.AUTH, code);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of("code", code);
    }
}
