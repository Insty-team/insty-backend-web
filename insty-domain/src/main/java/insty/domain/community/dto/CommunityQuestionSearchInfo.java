package insty.domain.community.dto;

import insty.domain.common.dto.UserInfo;
import insty.model.community.QuestionStatus;
import java.time.Instant;

public record CommunityQuestionSearchInfo(
        Long id,
        UserInfo user,
        Long courseId,
        String title,
        String content,
        QuestionStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
