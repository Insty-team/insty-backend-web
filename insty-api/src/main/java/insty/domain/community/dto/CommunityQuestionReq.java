package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;

public record CommunityQuestionReq(
        @NotNull
        Long courseId,
        @NotNull
        Long userId,
        Long questionId,
        @NotNull
        String title,
        @NotNull
        String content
) {

        public static CommunityQuestionReq create(
                Long courseId,
                Long userId,
                String title,
                String content
        ) {
            return new CommunityQuestionReq(courseId, userId, title, content);
        }
}
