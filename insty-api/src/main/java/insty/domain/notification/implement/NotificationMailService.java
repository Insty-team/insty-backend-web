package insty.domain.notification.implement;

import insty.domain.notification.service.NotificationService;
import insty.global.property.AppProperties;
import insty.mail.MailHelper;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.mention.Mention;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationMailService implements NotificationService {

    private final AppProperties appProperties;
    private final MailHelper mailHelper;

    @Override
    public void sendQuestionNotificationToCreator(CommunityQuestion question) {
        String creatorEmail = question.getCourse().getUser().getEmail();
        String questionTitle = question.getTitle();
        String questionContent = truncateContent(question.getContent(), appProperties.getMailPreviewLength());
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

    @Override
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

    @Override
    public void sendMentionNotification(List<Mention> mentions, String questionTitle) {
        for (Mention mention : mentions) {
            String mentionedUserEmail = mention.getMentionedUser().getEmail();
            String mentionerName = mention.getMentionerUser().getNickname();
            String questionUrl = generateQuestionUrl(mention.getCommunityAnswer().getCommunityQuestion().getId());

            MentionMailContent mailContent = MentionMailContent.of(
                    mentionedUserEmail,
                    questionTitle,
                    mentionerName,
                    questionUrl
            );

            mailHelper.send(mailContent);
        }
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
