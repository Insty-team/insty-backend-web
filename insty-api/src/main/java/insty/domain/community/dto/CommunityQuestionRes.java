package insty.domain.community.dto;

import insty.domain.common.FileInfo;
import insty.domain.community.implement.CommunityFileManager;
import insty.domain.community.implement.CommunityReader;
import insty.model.community.CommunityQuestion;
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
        List<CommunityAnswerRes> answers,
        List<FileInfo> attachments,
        CommunityAnswerRes acceptedAnswer
) {
    public static CommunityQuestionRes create(
            Long userId,
            Long courseId,
            String title,
            String content,
            Instant createdAt,
            Instant updatedAt,
            List<CommunityAnswerRes> answers,
            List<FileInfo> attachments,
            CommunityAnswerRes acceptedAnswer
    ) {
        return new CommunityQuestionRes(
                userId,
                courseId,
                title,
                content,
                createdAt,
                updatedAt,
                answers,
                attachments,
                acceptedAnswer
        );
    }

    public static CommunityQuestionRes from(CommunityQuestion question, CommunityReader communityReader, CommunityFileManager communityFileManager) {
        // 1. 답변 목록 생성 (각 답변의 첨부 파일 포함)
        List<CommunityAnswerRes> answers = question.getAnswers().stream()
                .map(answer -> {
                    List<insty.model.community.CommunityAnswerFile> answerFiles = communityReader.getCommunityAnswerFilesByAnswerId(answer.getId().toString());
                    List<insty.domain.common.FileInfo> fileInfos = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);
                    return CommunityAnswerRes.create(
                            answer.getUser().getId(),
                            answer.getContent(),
                            fileInfos,
                            answer.getCreatedAt(),
                            answer.getUpdatedAt(),
                            answer.isAccepted()
                    );
                })
                .toList();

        // 2. 질문 첨부 파일 처리
        List<insty.domain.common.FileInfo> questionAttachments = communityFileManager.convertToFileInfos(question.getAttachments());

        // 3. 채택된 답변 처리 (있는 경우에만)
        CommunityAnswerRes acceptedAnswerRes = null;
        if (question.getAcceptedAnswer() != null) {
            var acceptedAnswer = question.getAcceptedAnswer();
            List<insty.model.community.CommunityAnswerFile> acceptedAnswerFiles = communityReader.getCommunityAnswerFilesByAnswerId(acceptedAnswer.getId().toString());
            List<insty.domain.common.FileInfo> acceptedAnswerFileInfos = communityFileManager.convertAnswerFilesToFileInfos(acceptedAnswerFiles);
            acceptedAnswerRes = CommunityAnswerRes.create(
                    acceptedAnswer.getUser().getId(),
                    acceptedAnswer.getContent(),
                    acceptedAnswerFileInfos,
                    acceptedAnswer.getCreatedAt(),
                    acceptedAnswer.getUpdatedAt(),
                    acceptedAnswer.isAccepted()
            );
        }

        // 4. 최종 응답 데이터 생성
        return CommunityQuestionRes.create(
                question.getUser().getId(),
                question.getCourse().getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                answers,
                questionAttachments,
                acceptedAnswerRes
        );
    }
}
