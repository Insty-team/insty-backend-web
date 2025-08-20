package insty.domain.notification.content;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;
import java.util.Objects;

public final class MentionMailContent extends MailContent {

    private final String questionTitle;
    private final String mentionerName;
    private final String questionUrl;

    private MentionMailContent(String to, String questionTitle, String mentionerName, String questionUrl) {
        super(to, MailType.MENTION);
        this.questionTitle = Objects.requireNonNull(questionTitle);
        this.mentionerName = Objects.requireNonNull(mentionerName);
        this.questionUrl = Objects.requireNonNull(questionUrl);
    }

    public static MentionMailContent of(String to, String questionTitle, String mentionerName, String questionUrl) {
        return new MentionMailContent(to, questionTitle, mentionerName, questionUrl);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of(
                "questionTitle", questionTitle,
                "mentionerName", mentionerName,
                "questionUrl", questionUrl
        );
    }
}
