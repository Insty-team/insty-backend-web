package insty.domain.notification.implement;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;
import java.util.Objects;

public final class CommunityQuestionMailContent extends MailContent {

    private final String questionTitle;
    private final String questionContent;
    private final String questionAuthorName;
    private final String courseName;
    private final String questionUrl;

    private CommunityQuestionMailContent(String to, String questionTitle, String questionContent,
                                         String questionAuthorName, String courseName, String questionUrl) {
        super(to, MailType.COMMUNITY_QUESTION);
        this.questionTitle = Objects.requireNonNull(questionTitle);
        this.questionContent = Objects.requireNonNull(questionContent);
        this.questionAuthorName = Objects.requireNonNull(questionAuthorName);
        this.courseName = Objects.requireNonNull(courseName);
        this.questionUrl = Objects.requireNonNull(questionUrl);
    }

    public static CommunityQuestionMailContent of(String to, String questionTitle, String questionContent,
                                                  String questionAuthorName, String courseName, String questionUrl) {
        return new CommunityQuestionMailContent(to, questionTitle, questionContent, questionAuthorName, courseName, questionUrl);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of(
                "questionTitle", questionTitle,
                "questionContent", questionContent,
                "questionAuthorName", questionAuthorName,
                "courseName", courseName,
                "questionUrl", questionUrl
        );
    }
}
