package insty.domain.notification.implement;

import insty.domain.notification.content.MentionMailContent;
import insty.domain.notification.util.NotificationUtils;
import insty.mail.MailHelper;
import insty.model.mention.Mention;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MentionNotificationService {

    private final NotificationUtils notificationUtils;
    private final MailHelper mailHelper;
    private final NotificationSettingService notificationSettingService;

    public void sendMentionNotification(Mention mention, String questionTitle) {
        Long mentionedUserId = mention.getMentionedUser().getId();

        if (!notificationSettingService.isEmailNotificationEnabled(mentionedUserId)) {
            return;
        }

        String mentionedUserEmail = mention.getMentionedUser().getEmail();
        String mentionerName = mention.getMentionerUser().getNickname();
        String questionUrl = generateQuestionUrl(mention.getCommunityAnswer().getCommunityQuestion().getId());

        MentionMailContent mailContent = MentionMailContent.of(
                mentionedUserEmail,
                mentionerName,
                questionTitle,
                questionUrl
        );

        mailHelper.send(mailContent);
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", notificationUtils.getDomain(), questionId);
    }
}
