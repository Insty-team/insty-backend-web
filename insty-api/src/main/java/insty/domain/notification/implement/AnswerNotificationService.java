package insty.domain.notification.implement;

import insty.domain.notification.content.CommunityAnswerMailContent;
import insty.global.property.AppProperties;
import insty.mail.MailHelper;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerNotificationService {

    private final AppProperties appProperties;
    private final MailHelper mailHelper;

    public void sendAnswerNotification(CommunityQuestion question, CommunityAnswer answer) {
        String receiverEmail = determineAnswerNotificationReceiver(question, answer);
        String questionTitle = question.getTitle();
        String answerContent = truncateContent(answer.getContent(), appProperties.getMailPreviewLength());
        String answerAuthorName = answer.getUser().getNickname();
        String questionUrl = generateQuestionUrl(question.getId());

        CommunityAnswerMailContent mailContent = CommunityAnswerMailContent.of(
                receiverEmail,
                questionTitle,
                answerContent,
                answerAuthorName,
                questionUrl
        );

        mailHelper.send(mailContent);
    }

    private String determineAnswerNotificationReceiver(CommunityQuestion question, CommunityAnswer answer) {
        Long answererUserId = answer.getUser().getId();
        Long courseCreatorId = question.getCourse().getUser().getId();

        // Creator가 답변한 경우 → Runner(질문 작성자)에게 전송
        if (answererUserId.equals(courseCreatorId)) {
            return question.getUser().getEmail();
        }
        // Runner가 답변한 경우 → Creator에게 전송
        else {
            return question.getCourse().getUser().getEmail();
        }
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
        return String.format("%s/community/questions/%d", appProperties.getDomain(), questionId);
    }
}
