package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.model.community.CommunityQuestion;

import java.time.Instant;
import java.util.List;

import java.util.Optional;

public record CommunityQuestionRes(
        Long userId,
        Long courseId,
        String title,
        String content,
        Instant createdAt,
        Instant updatedAt,
        List<CommunityAnswerRes> answers,
        List<FileInfo> attachments
) {
    public static CommunityQuestionRes from(
            CommunityQuestion question,
            List<FileInfo> attachments,
            List<CommunityAnswerRes> answers
    ) {
        return new CommunityQuestionRes(
                question.getUser().getId(),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                Optional.ofNullable(answers).orElse(List.of()),
                Optional.ofNullable(attachments).orElse(List.of())
        );
    }
}
