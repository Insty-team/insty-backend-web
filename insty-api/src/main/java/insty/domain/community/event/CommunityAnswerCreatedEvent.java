package insty.domain.community.event;

public record CommunityAnswerCreatedEvent(
        Long questionId,
        Long answerId
) {
}