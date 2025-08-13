package insty.domain.community.dto;

import insty.model.community.QuestionStatus;
import java.util.List;
import lombok.Builder;

@Builder
public record CommunityQuestionSearchFilter(
        String query,
        List<QuestionStatus> statuses,
        Long courseId,
        Long userId
) {}