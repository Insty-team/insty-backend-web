package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.common.VideoInfo;
import insty.model.community.CommunityQuestion;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public record CommunityQuestionDetailsRes(
        CommunityUserRes user,
        Long courseId,
        String title,
        String content,
        List<CommunityAnswerRes> answers,
        List<FileInfo> attachments,
        VideoInfo videoInfo,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityQuestionDetailsRes from(
            CommunityQuestion question,
            List<FileInfo> attachments,
            VideoInfo videoInfo,
            List<CommunityAnswerRes> answers
    ) {
        return new CommunityQuestionDetailsRes(
                CommunityUserRes.from(question.getUser()),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                Optional.ofNullable(answers).orElse(List.of()),
                Optional.ofNullable(attachments).orElse(List.of()),
                videoInfo,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
