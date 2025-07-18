package insty.domain.community.dto;

import jakarta.validation.constraints.NotNull;


public record CommunityQuestionUpdateReq(
        Long questionId,
        @NotNull
        String title,
        @NotNull
        String content
) {

        public static CommunityQuestionUpdateReq create(
                Long questionId,
                String title,
                String content
        ) {
            return new CommunityQuestionUpdateReq(questionId, title, content);
        }
}
