package insty.domain.user.dto.response;

import insty.domain.user.dto.UserAuthTokenDto;
import insty.model.user.UserType;

public record AuthUserRes(
        Long id,
        String nickname,
        UserType userType,
        UserAuthTokenDto token
) {
    public static AuthUserRes create(
            Long id,
            String nickname,
            UserType userType,
            UserAuthTokenDto token
    ) {
        return new AuthUserRes(id, nickname, userType, token);
    }
}
