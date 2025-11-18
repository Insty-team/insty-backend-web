package insty.mail.content;

import insty.mail.MailType;
import insty.mail.MailContent;
import java.util.Map;
import java.util.Objects;

public final class NewAnswerMailContent extends MailContent {

    private final String questionTitle;
    private final String answerContent;
    private final String answerAuthorNickname;
    private final String questionUrl;

    private NewAnswerMailContent(String to, String questionTitle, String answerContent, String answerAuthorNickname, String questionUrl) {
        super(to, MailType.COMMUNITY_ANSWER);
        this.questionTitle = Objects.requireNonNull(questionTitle);
        this.answerContent = Objects.requireNonNull(answerContent);
        this.answerAuthorNickname = Objects.requireNonNull(answerAuthorNickname);
        this.questionUrl = Objects.requireNonNull(questionUrl);
    }

    public static NewAnswerMailContent of(String to, String questionTitle, String answerContent, String answerAuthorNickname, String questionUrl) {
        return new NewAnswerMailContent(to, questionTitle, answerContent, answerAuthorNickname, questionUrl);
    }

    @Override
    public Map<String, Object> variables() {
        return Map.of(
                "questionTitle", questionTitle,
                "answerContent", answerContent,
                "answerAuthorNickname", answerAuthorNickname,
                "questionUrl", questionUrl
        );
    }
}
