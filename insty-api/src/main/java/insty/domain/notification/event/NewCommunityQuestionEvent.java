package insty.domain.notification.event;

import insty.model.community.CommunityQuestion;

public record NewCommunityQuestionEvent(
        CommunityQuestion question
) {
}
