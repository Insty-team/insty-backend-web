package insty.domain.courseqna.dto;

import insty.model.courseqna.QuestionStatus;
import java.util.List;
public record CourseQuestionSearchFilter(
        String query,
        List<QuestionStatus> statuses,
        Long courseId,
        Long userId,
        String boardType
) {}
