package insty.domain.notification.event;

import insty.model.community.CommunityQuestion;
import insty.model.user.User;

public record UserMentionedEvent(
        User sender,
        User receiver,
        CommunityQuestion question
) {
}
