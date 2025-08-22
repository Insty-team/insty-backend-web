package insty.domain.notification.event;

import insty.model.community.CommunityQuestion;
import insty.model.course.Course;
import insty.model.user.User;

public record NewCommunityQuestionEvent(
        User receiver,
        User questionAuthor,
        CommunityQuestion question,
        Course course
) {
}
