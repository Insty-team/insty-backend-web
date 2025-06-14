package insty.domain.user.dto.request;

import insty.model.user.UserType;

public record UserSocialLoginReq(
        String code,
        String state,
        UserType userType
) {
}
