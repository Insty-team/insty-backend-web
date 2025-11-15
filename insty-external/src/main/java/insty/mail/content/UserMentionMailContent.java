package insty.mail.content;

import insty.mail.MailType;
import insty.mail.MailContent;
import java.util.Map;
import java.util.Objects;

public final class UserMentionMailContent extends MailContent {

    private final String questionTitle;
    private final String mentionerName;
    private final String questionUrl;

    private UserMentionMailContent(String to, String questionTitle, String mentionerName, String questionUrl) {
        super(to, MailType.MENTION);
        this.questionTitle = Objects.requireNonNull(questionTitle);
        this.mentionerName = Objects.requireNonNull(mentionerName);
        this.questionUrl = Objects.requireNonNull(questionUrl);
    }

    public static UserMentionMailContent of(String to, String questionTitle, String mentionerName, String questionUrl) {
        return new UserMentionMailContent(to, questionTitle, mentionerName, questionUrl);
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
