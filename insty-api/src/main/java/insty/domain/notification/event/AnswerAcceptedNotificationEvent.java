package insty.domain.notification.event;

import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityQuestion;
import insty.model.user.User;

public record AnswerAcceptedNotificationEvent(
        User receiver,
        CommunityQuestion question,
        CommunityAnswer answer
) {
}
