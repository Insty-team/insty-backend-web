package insty.domain.mention.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MentionedUserInfo(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        
        @Schema(description = "멘션에서 사용된 표시명", example = "홍길동")
        String displayName
) {
    
    public MentionedUserInfo(Long userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }
}
