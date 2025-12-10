package insty.domain.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetMailReq(
        @Pattern(regexp="^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                 message="이메일 주소 양식을 확인해주세요")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email
) {
}
