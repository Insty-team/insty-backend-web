package insty.domain.notification.event;

import insty.model.mention.Mention;

public record MentionCreatedEvent(
        Mention mention,
        String questionTitle
) {
}
