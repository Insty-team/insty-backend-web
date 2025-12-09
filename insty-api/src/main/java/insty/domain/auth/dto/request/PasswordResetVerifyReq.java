package insty.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetVerifyReq(
        @Pattern(regexp="^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$",
                 message="이메일 주소 양식을 확인해주세요")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "인증 코드를 입력해주세요.")
        String code
) {
}