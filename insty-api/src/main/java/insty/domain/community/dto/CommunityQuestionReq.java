package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityQuestionReq(
        Long questionId,
        @NotNull
        Long courseId,
        @NotNull
        Long userId,
        @NotNull
        String title,
        @NotNull
        String content
) {

        public static CommunityQuestionReq create(
                Long questionId,
                Long courseId,
                Long userId,
                String title,
                String content
        ) {
            return new CommunityQuestionReq(questionId, courseId, userId, title, content);
        }
}
