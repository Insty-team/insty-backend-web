package insty.domain.mention.implement;

import insty.model.courseqna.CourseQuestion;
import insty.model.mention.Mention;
import insty.domain.notification.dto.event.NotificationReq;
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

    public void sendMentionsNotification(List<Mention> mentions, CourseQuestion courseQuestion) {
        if (mentions == null || mentions.isEmpty()) {
            return;
        }

        for (Mention mention : mentions) {
            if (mention.getCourseAnswer() == null) {
                continue;
            }

            NotificationReq request = NotificationReq.userMentioned(
                    mention.getMentionedUser().getId(),
                    mention.getId(),
                    mention.getMentionerUser().getNickname(),
                    mention.getCourseAnswer().getContent(),
                    "ANSWER",
                    mention.getCourseAnswer().getId()
            );

            eventPublisher.publishEvent(request);
        }
    }
}
