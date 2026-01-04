package insty.domain.user.dto.request;

import insty.model.user.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSocialLoginReq(
        @NotBlank(message = "소셜로그인 Code 값은 필수입니다.")
        String code,

        //@NotNull(message = "회원 유형은 필수입니다.")
        UserType userType
) {
}
