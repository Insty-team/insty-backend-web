package insty.domain.notification.implement;

import insty.domain.notification.content.CommunityAnswerMailContent;
import insty.domain.notification.util.NotificationUtils;
import insty.mail.MailHelper;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnswerNotificationService {

    private final NotificationUtils notificationUtils;
    private final MailHelper mailHelper;
    private final NotificationSettingService notificationSettingService;

    public void sendAnswerNotification(CommunityQuestion question, CommunityAnswer answer) {
        AnswerReceiver receiver = determineAnswerReceiver(question, answer);
        
        if (!notificationSettingService.isEmailNotificationEnabled(receiver.userId())) {
            return;
        }
        
        String questionTitle = question.getTitle();
        String answerContent = notificationUtils.truncateContent(answer.getContent(), notificationUtils.getDefaultPreviewLength());
        String answerAuthorName = answer.getUser().getNickname();
        String questionUrl = generateQuestionUrl(question.getId());

        CommunityAnswerMailContent mailContent = CommunityAnswerMailContent.of(
                receiver.email(),
                questionTitle,
                answerContent,
                answerAuthorName,
                questionUrl
        );

        mailHelper.send(mailContent);
    }

    private AnswerReceiver determineAnswerReceiver(CommunityQuestion question, CommunityAnswer answer) {
        Long answererUserId = answer.getUser().getId();
        Long courseCreatorId = question.getCourse().getUser().getId();

        // Creator가 답변한 경우 → Runner(질문 작성자)에게 전송
        if (answererUserId.equals(courseCreatorId)) {
            return new AnswerReceiver(question.getUser().getId(), question.getUser().getEmail());
        }
        // Runner가 답변한 경우 → Creator에게 전송
        else {
            return new AnswerReceiver(question.getCourse().getUser().getId(), question.getCourse().getUser().getEmail());
        }
    }

    private record AnswerReceiver(Long userId, String email) {}

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
