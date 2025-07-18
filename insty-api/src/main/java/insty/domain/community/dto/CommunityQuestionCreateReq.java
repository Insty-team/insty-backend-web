package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;


public record CommunityQuestionCreateReq(
        @NotNull
        Long courseId,
        @NotNull
        Long userId,
        @NotNull
        String title,
        @NotNull
        String content
) {

        public static CommunityQuestionCreateReq create(
                Long courseId,
                Long userId,
                String title,
                String content
        ) {
            return new CommunityQuestionCreateReq(courseId, userId, title, content);
        }
}
