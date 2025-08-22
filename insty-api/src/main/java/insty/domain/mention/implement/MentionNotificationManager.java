package insty.domain.mention.implement;

import insty.domain.notification.event.UserMentionedEvent;
import insty.model.community.CommunityQuestion;
import insty.model.mention.Mention;
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
            eventPublisher.publishEvent(
                    new UserMentionedEvent(mention.getMentionedUser(), mention.getMentionerUser(), communityQuestion));
        }
    }
}
