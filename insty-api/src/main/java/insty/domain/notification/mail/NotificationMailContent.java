package insty.domain.notification.mail;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;

public class NotificationMailContent extends MailContent {

    private final Map<String, Object> variables;

    public NotificationMailContent(String to, MailType mailType, Map<String, Object> variables) {
        super(to, mailType);
        this.variables = variables;
    }

    @Override
    public Map<String, Object> variables() {
        return variables;
    }

    public static NotificationMailContent of(String to, MailType mailType, Map<String, Object> context) {
        return new NotificationMailContent(to, mailType, context);
    }
}
