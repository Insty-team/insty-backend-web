package insty.domain.notification.implement;

import insty.domain.notification.content.MentionMailContent;
import insty.global.property.AppProperties;
import insty.mail.MailHelper;
import insty.model.mention.Mention;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentionNotificationService {

    private final AppProperties appProperties;
    private final MailHelper mailHelper;

    public void sendMentionNotification(List<Mention> mentions, String questionTitle) {
        for (Mention mention : mentions) {
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
    }

    private String generateQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", appProperties.getDomain(), questionId);
    }
}
