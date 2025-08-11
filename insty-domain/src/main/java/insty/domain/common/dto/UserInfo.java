package insty.domain.common.dto;

import insty.model.user.User;
import insty.model.user.UserType;

public record UserInfo(
        Long id,
        String nickname,
        UserType userType
) {

    public static UserInfo from(User user) {
        return new UserInfo(user.getId(), user.getNickname(), user.getUserType());
    }
}
