package insty.domain.community.dto;

import insty.domain.common.dto.UserInfo;
import java.time.Instant;

public record CommunityQuestionSearchInfo(
        Long id,
        UserInfo user,
        Long courseId,
        String title,
        String content,
        boolean isAnswered,
        Instant createdAt,
        Instant updatedAt
) {}
