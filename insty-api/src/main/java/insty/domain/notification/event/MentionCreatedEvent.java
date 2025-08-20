package insty.domain.notification.event;

import insty.model.mention.Mention;
import java.util.List;

public record MentionCreatedEvent(
        List<Mention> mentions,
        String questionTitle
) {
}
