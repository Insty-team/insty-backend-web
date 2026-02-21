package insty.domain.mention.implement;

import insty.domain.mention.dto.MentionCreateEvent;
import insty.model.mention.MentionTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MentionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(Long mentionerUserId, MentionTargetType targetType, Long targetId, String content) {
        if (mentionerUserId == null || targetType == null || targetId == null || content == null || content.isBlank()) {
            return;
        }
        eventPublisher.publishEvent(MentionCreateEvent.of(mentionerUserId, targetType, targetId, content));
    }
}
