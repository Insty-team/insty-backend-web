package insty.domain.mention.implement;

import insty.domain.notification.dto.event.NotificationReq;
import insty.model.mention.Mention;
import insty.model.mention.MentionTargetType;
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

    public void sendMentionsNotification(List<Mention> mentions, String content, MentionTargetType targetType, Long targetId) {
        if (mentions == null || mentions.isEmpty()) {
            return;
        }

        for (Mention mention : mentions) {
            NotificationReq request = NotificationReq.userMentioned(
                    mention.getMentionedUser().getId(),
                    mention.getId(),
                    mention.getMentionerUser().getNickname(),
                    content,
                    targetType.toNotificationContentType(),
                    targetId
            );

            eventPublisher.publishEvent(request);
        }
    }

    @Deprecated
    public void sendMentionsNotification(List<Mention> mentions, insty.model.courseqna.CourseQuestion courseQuestion) {
        if (courseQuestion == null) {
            return;
        }
        sendMentionsNotification(mentions, "", MentionTargetType.COURSE_QUESTION, courseQuestion.getId());
    }
}
