package insty.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetVerifyReq(
        @Pattern(regexp="^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,15}$",
                 message="이메일 주소 양식을 확인해주세요")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "인증 코드를 입력해주세요.")
        @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.")
        String code
) {
}