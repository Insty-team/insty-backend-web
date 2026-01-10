package insty.domain.user.dto.request;

import insty.model.user.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserLoginReq (

        @Schema(description = "이메일 주소", example = "youremail@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        /*
            (?=.*[A-Za-z])      // 영문이 있나?
            (?=.*\d)            // 숫자가 있나?
            (?=.*[!@#$...])     // 특수문자가 있나?
         */
        @Schema(description = "비밀번호", example = "abc123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]).+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상이어야 합니다."
        )
        String password,

        @Schema(
                description = "회원 타입 (선택 사항, 곧 삭제 예정)",
                example = "LEARNER",
                nullable = true,
                deprecated = true
        )
        UserType userType
) {

}
