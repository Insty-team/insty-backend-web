package insty.domain.user.dto.request;

import insty.model.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UserSocialLoginReq(
        @NotBlank(message = "소셜로그인 Code 값은 필수입니다.")
        String code,

        @Schema(
                description = "회원 타입 (선택 사항, 곧 삭제 예정)",
                example = "LEARNER",
                nullable = true,
                deprecated = true
        )
        UserType userType
) {
}
