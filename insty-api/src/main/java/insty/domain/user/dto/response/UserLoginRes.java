package insty.domain.user.dto.response;

import insty.domain.user.dto.UserAuthTokenDto;
import insty.model.user.UserType;

public record UserLoginRes(
        Long id,
        String nickname,
        UserType userType,
        UserAuthTokenDto token
) {
    public static UserLoginRes create(
            Long id,
            String nickname,
            UserType userType,
            UserAuthTokenDto token
    ) {
        return new UserLoginRes(id, nickname, userType, token);
    }
}
