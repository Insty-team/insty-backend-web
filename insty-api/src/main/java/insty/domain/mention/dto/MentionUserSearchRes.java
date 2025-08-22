package insty.domain.mention.dto;

import insty.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record MentionUserSearchRes(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        
        @Schema(description = "사용자 닉네임 (표시명)", example = "홍길동")
        String nickname,
        
        @Schema(
                description = "사용자 타입 (LEARNER/CREATOR)",
                example = "CREATOR",
                allowableValues = {"LEARNER", "CREATOR"}
        )
        String userType,
        
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl
) {

    public static MentionUserSearchRes from(User user, String profileImageUrl){
        return new MentionUserSearchRes(
                user.getId(),
                user.getNickname(),
                user.getUserType().name(),
                profileImageUrl
        );
    }
}
