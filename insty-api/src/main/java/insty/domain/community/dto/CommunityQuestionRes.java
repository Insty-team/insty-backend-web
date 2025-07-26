package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityQuestion;

import insty.model.user.User;
import java.time.Instant;
import java.util.List;

import java.util.Optional;

public record CommunityQuestionRes(
        CommunityUserRes user,
        Long courseId,
        String title,
        String content,
        Boolean isAnswered,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityQuestionRes from(
            CommunityQuestion question
    ) {
        return new CommunityQuestionRes(
                CommunityUserRes.from(question.getUser()),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                question.isAnswered(),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
