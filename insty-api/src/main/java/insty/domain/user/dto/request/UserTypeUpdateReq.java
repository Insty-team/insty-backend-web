package insty.domain.user.dto.request;

import insty.model.user.UserType;
import jakarta.validation.constraints.NotNull;

public record UserTypeUpdateReq(
        @NotNull(message = "회원 유형은 필수입니다.")
        UserType userType
) {

}
