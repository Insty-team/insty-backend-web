package insty.domain.community.dto;

import insty.model.user.User;
import insty.model.user.UserType;
import lombok.Builder;

@Builder
public record  CommunityUserRes(
        Long id,
        String nickname,
        UserType userType
) {
    public static CommunityUserRes from(User user) {
        return new CommunityUserRes(user.getId(), user.getNickname(), user.getUserType());
    }

}
