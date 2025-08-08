package insty.mail;

import java.util.Map;
import java.util.Objects;

public abstract class MailContent {

    protected final String to;
    protected final MailType mailType;

    protected MailContent(String to, MailType mailType) {
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("receiver mail 주소는 필수입니다.");
        }
        this.to = to;
        this.mailType = Objects.requireNonNull(mailType, "mailType은 null일 수 없습니다");
    }

    public final String to() { return to; }
    public final MailType mailType() { return mailType; }
    public abstract Map<String, Object> variables();
}
