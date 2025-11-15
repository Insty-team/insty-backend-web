package insty.mail.content;

import insty.mail.MailContent;
import insty.mail.MailType;

import java.util.Map;

/**
 * 알림용 메일 컨텐츠
 * NotificationRequest의 context를 그대로 템플릿 변수로 전달
 */
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

    /**
     * 빌더 패턴으로 생성
     */
    public static NotificationMailContent of(String to, MailType mailType, Map<String, Object> context) {
        return new NotificationMailContent(to, mailType, context);
    }
}
