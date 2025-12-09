package insty.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateReq(
        @Pattern(regexp="^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])+[.][a-zA-Z]{2,3}$",
                 message="이메일 주소 양식을 확인해주세요")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Pattern(regexp="^(?=.*[A-Z])(?=.*[0-9])(?=.*[a-z])(?=.*[!@#$%^&*()-+=]).{8,}$\n" ,
                 message="비밀번호 양식을 확인해주세요")
        String newPassword
) {
}