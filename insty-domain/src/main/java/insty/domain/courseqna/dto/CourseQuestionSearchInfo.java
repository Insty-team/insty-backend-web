package insty.domain.courseqna.dto;

import insty.domain.common.dto.UserInfo;
import insty.model.courseqna.QuestionStatus;
import java.time.Instant;

public record CourseQuestionSearchInfo(
        Long id,
        UserInfo user,
        Long courseId,
        String title,
        String content,
        QuestionStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
