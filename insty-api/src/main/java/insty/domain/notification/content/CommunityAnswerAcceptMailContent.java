package insty.domain.notification.content;

import insty.mail.MailContent;
import insty.mail.MailType;
import java.util.Map;
import java.util.Objects;

public final class CommunityAnswerAcceptMailContent extends MailContent {

    private final String questionTitle;
    private final String answerContent;
    private final String answerAuthorName;
    private final String questionAuthorName;
    private final String questionUrl;

    private CommunityAnswerAcceptMailContent(String to, String questionTitle, String answerContent,
                                             String answerAuthorName, String questionAuthorName, String questionUrl) {
        super(to, MailType.COMMUNITY_ANSWER_ACCEPT);
        this.questionTitle = Objects.requireNonNull(questionTitle);
        this.answerContent = Objects.requireNonNull(answerContent);
        this.answerAuthorName = Objects.requireNonNull(answerAuthorName);
        this.questionAuthorName = Objects.requireNonNull(questionAuthorName);
        this.questionUrl = Objects.requireNonNull(questionUrl);
    }

    public static CommunityAnswerAcceptMailContent of(String to, String questionTitle, String answerContent,
                                                      String answerAuthorName, String questionAuthorName,
                                                      String questionUrl) {
        return new CommunityAnswerAcceptMailContent(to, questionTitle, answerContent, answerAuthorName,
                questionAuthorName, questionUrl);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of(
                "questionTitle", questionTitle,
                "answerContent", answerContent,
                "answerAuthorName", answerAuthorName,
                "questionAuthorName", questionAuthorName,
                "questionUrl", questionUrl
        );
    }
}
