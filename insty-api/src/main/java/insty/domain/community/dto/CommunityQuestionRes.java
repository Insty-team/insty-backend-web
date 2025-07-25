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
        List<CommunityAnswerRes> answers,
        List<FileInfo> attachments,
        List<VideoInfo> videoInfos,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommunityQuestionRes from(
            CommunityQuestion question,
            List<FileInfo> attachments,
            List<VideoInfo> videoInfos,
            List<CommunityAnswerRes> answers
    ) {
        return new CommunityQuestionRes(
                CommunityUserRes.from(question.getUser()),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                Optional.ofNullable(answers).orElse(List.of()),
                Optional.ofNullable(attachments).orElse(List.of()),
                videoInfos,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }
}
