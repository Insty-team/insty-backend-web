package insty.domain.auth.implement.emailverification;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;
import java.util.Objects;

public final class MailAuthenticateContent extends MailContent  {

    private final String code;

    private MailAuthenticateContent(String to, String code) {
        super(to, MailType.AUTH);
        this.code = Objects.requireNonNull(code);
    }

    public static MailAuthenticateContent of(String to, String code) {
        return new MailAuthenticateContent(to, code);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of("code", code);
    }
}
