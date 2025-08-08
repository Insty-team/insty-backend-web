package insty.domain.community.implement.mail;

import insty.mail.MailHelper;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityMailService {

    private final MailHelper mailHelper;

    public void sendQuestionNotificationToCreator(CommunityQuestion question) {
        String creatorEmail = question.getCourse().getUser().getEmail();
        String questionTitle = question.getTitle();
        String questionContent = truncateContent(question.getContent(), 100);
        String questionAuthorName = question.getUser().getNickname();
        String courseName = question.getCourse().getTitle();
        String questionUrl = generateQuestionUrl(question.getId());

        CommunityQuestionMailContent mailContent = CommunityQuestionMailContent.of(
                creatorEmail,
                questionTitle,
                questionContent,
                questionAuthorName,
                courseName,
                questionUrl
        );

        mailHelper.send(mailContent);
    }

    public void sendAnswerNotification(CommunityQuestion question, CommunityAnswer answer) {
        String questionAuthorEmail = question.getUser().getEmail();
        String questionTitle = question.getTitle();
        String answerContent = truncateContent(answer.getContent(), 100);
        String answerAuthorName = answer.getUser().getNickname();
        String questionUrl = generateQuestionUrl(question.getId());

        CommunityAnswerMailContent mailContent = CommunityAnswerMailContent.of(
                questionAuthorEmail,
                questionTitle,
                answerContent,
                answerAuthorName,
                questionUrl
        );

        mailHelper.send(mailContent);
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("/community/questions/%d", questionId);
    }
}
