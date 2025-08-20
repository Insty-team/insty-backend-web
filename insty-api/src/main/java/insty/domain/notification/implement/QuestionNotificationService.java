package insty.domain.notification.implement;

import insty.domain.notification.content.CommunityQuestionMailContent;
import insty.domain.notification.util.NotificationUtils;
import insty.mail.MailHelper;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionNotificationService {

    private final NotificationUtils notificationUtils;
    private final MailHelper mailHelper;
    private final NotificationSettingService notificationSettingService;

    public void sendQuestionNotificationToCreator(CommunityQuestion question) {
        Long creatorId = question.getCourse().getUser().getId();
        
        if (!notificationSettingService.isEmailNotificationEnabled(creatorId)) {
            return;
        }
        
        String creatorEmail = question.getCourse().getUser().getEmail();
        String questionTitle = question.getTitle();
        String questionContent = notificationUtils.truncateContent(question.getContent(), notificationUtils.getDefaultPreviewLength());
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

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
