package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record CommunityQuestionRes(
        Long userId,
        Long courseId,
        @NotNull
        String title,
        @NotNull
        String content,
        Instant createdAt,
        Instant updatedAt,
        List<CommunityAnswerRes> answers
        // TODO: List<CommunityAttachmentRes> attachments
) {
    public static CommunityQuestionRes create(
            Long userId,
            Long courseId,
            String title,
            String content,
            Instant createdAt,
            Instant updatedAt,
            List<CommunityAnswerRes> answers
            //List<CommunityAttachmentRes> attachments
    ) {
        return new CommunityQuestionRes(
                userId,
                courseId,
                title,
                content,
                createdAt,
                updatedAt,
                answers
                //attachments
        );
    }
}
