package insty.domain.community.dto;

import insty.domain.common.dto.UserInfo;
import insty.model.user.User;
import insty.model.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public record  CommunityUserRes(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 닉네임", example = "김철수")
        String nickname,

        @Schema(description = "사용자 타입", example = "LEARNER")
        UserType userType
) {
    public static CommunityUserRes from(User user) {
        return new CommunityUserRes(user.getId(), user.getNickname(), user.getUserType());
    }

    public static CommunityUserRes from(UserInfo userInfo) {
        return new CommunityUserRes(userInfo.id(), userInfo.nickname(), userInfo.userType());
    }
}
