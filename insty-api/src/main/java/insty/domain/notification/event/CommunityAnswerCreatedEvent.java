package insty.domain.notification.event;

public record CommunityAnswerCreatedEvent(
        Long questionId,
        Long answerId
) {
}
