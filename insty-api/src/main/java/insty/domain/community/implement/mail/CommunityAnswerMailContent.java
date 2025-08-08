package insty.domain.community.implement.mail;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;
import java.util.Objects;

public final class CommunityAnswerMailContent extends MailContent {

    private final String questionTitle;
    private final String answerContent;
    private final String answerAuthorName;
    private final String questionUrl;

    private CommunityAnswerMailContent(String to, String questionTitle, String answerContent,
                                       String answerAuthorName, String questionUrl) {
        super(to, MailType.COMMUNITY_ANSWER);
        this.questionTitle = Objects.requireNonNull(questionTitle);
        this.answerContent = Objects.requireNonNull(answerContent);
        this.answerAuthorName = Objects.requireNonNull(answerAuthorName);
        this.questionUrl = Objects.requireNonNull(questionUrl);
    }

    public static CommunityAnswerMailContent of(String to, String questionTitle, String answerContent,
                                                String answerAuthorName, String questionUrl) {
        return new CommunityAnswerMailContent(to, questionTitle, answerContent, answerAuthorName, questionUrl);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of(
                "questionTitle", questionTitle,
                "answerContent", answerContent,
                "answerAuthorName", answerAuthorName,
                "questionUrl", questionUrl
        );
    }
}