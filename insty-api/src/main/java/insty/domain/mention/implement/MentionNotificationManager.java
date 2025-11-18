package insty.domain.mention.implement;

import insty.model.community.CommunityQuestion;
import insty.model.mention.Mention;
import insty.domain.notification.dto.NotificationRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentionNotificationManager {

    private final ApplicationEventPublisher eventPublisher;

    public void sendMentionsNotification(List<Mention> mentions, CommunityQuestion communityQuestion) {
        if (mentions == null || mentions.isEmpty()) {
            return;
        }

        for (Mention mention : mentions) {
            if (mention.getCommunityAnswer() == null) {
                continue;
            }

            NotificationRequest request = NotificationRequest.userMentioned(
                    mention.getMentionedUser().getId(),
                    mention.getId(),
                    mention.getMentionerUser().getNickname(),
                    mention.getCommunityAnswer().getContent(),
                    "ANSWER",
                    mention.getCommunityAnswer().getId()
            );

            eventPublisher.publishEvent(request);
        }
    }
}
