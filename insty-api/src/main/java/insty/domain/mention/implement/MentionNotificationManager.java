package insty.domain.mention.implement;

import insty.model.community.CommunityQuestion;
import insty.model.mention.Mention;
import insty.notification.NotificationRequest;
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
            String content = mention.getCommunityAnswer() != null
                    ? mention.getCommunityAnswer().getContent()
                    : "";

            NotificationRequest request = NotificationRequest.userMentioned(
                    mention.getMentionedUser().getId(),
                    mention.getId(),
                    mention.getMentionerUser().getNickname(),
                    content,
                    "ANSWER",
                    mention.getCommunityAnswer().getId()
            );

            eventPublisher.publishEvent(request);
        }
    }
}
