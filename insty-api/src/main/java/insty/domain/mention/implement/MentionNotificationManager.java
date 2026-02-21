package insty.domain.mention.implement;

import insty.domain.courseqna.implement.CourseAnswerReader;
import insty.domain.notification.dto.event.NotificationReq;
import insty.exception.CustomException;
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

    private final CourseAnswerReader courseAnswerReader;
    private final ApplicationEventPublisher eventPublisher;

    public void sendMentionsNotification(List<Mention> mentions, String content, MentionTargetType targetType, Long targetId) {
        if (mentions == null || mentions.isEmpty()) {
            return;
        }

        Long courseQuestionId = resolveCourseQuestionId(targetType, targetId);

        for (Mention mention : mentions) {
            NotificationReq request = createNotificationRequest(mention, content, targetType, targetId, courseQuestionId);

            eventPublisher.publishEvent(request);
        }
    }

    private NotificationReq createNotificationRequest(
            Mention mention, String content, MentionTargetType targetType, Long targetId, Long courseQuestionId
    ) {
        if (targetType == MentionTargetType.COURSE_ANSWER && courseQuestionId != null) {
            return NotificationReq.userMentioned(
                    mention.getMentionedUser().getId(),
                    mention.getId(),
                    mention.getMentionerUser().getNickname(),
                    content,
                    targetType.toNotificationContentType(),
                    courseQuestionId,
                    courseQuestionId,
                    targetId
            );
        }

        return NotificationReq.userMentioned(
                mention.getMentionedUser().getId(),
                mention.getId(),
                mention.getMentionerUser().getNickname(),
                content,
                targetType.toNotificationContentType(),
                targetId
        );
    }

    private Long resolveCourseQuestionId(MentionTargetType targetType, Long targetId) {
        if (targetType != MentionTargetType.COURSE_ANSWER || targetId == null) {
            return null;
        }

        try {
            return courseAnswerReader.getQuestionIdByAnswerId(targetId);
        } catch (CustomException e) {
            log.warn("멘션 URL 생성용 질문 ID 조회 실패 - answerId={}", targetId);
            return null;
        }
    }

}
