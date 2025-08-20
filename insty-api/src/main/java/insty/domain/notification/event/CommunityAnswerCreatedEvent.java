package insty.domain.notification.event;

import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;

public record CommunityAnswerCreatedEvent(
        CommunityQuestion question,
        CommunityAnswer answer
) {
}
