package insty.domain.notification.implement;

import insty.domain.notification.content.CommunityQuestionMailContent;
import insty.global.property.AppProperties;
import insty.mail.MailHelper;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionNotificationService {

    private final AppProperties appProperties;
    private final MailHelper mailHelper;

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
