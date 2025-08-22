package insty.domain.notification.event;

import insty.model.community.CommunityQuestion;
import insty.model.user.User;

public record UserMentionedEvent(
        User receiver,
        User sender,
        CommunityQuestion question
) {
}
