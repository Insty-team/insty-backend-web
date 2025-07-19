package insty.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateReq(

        /*
            (?=.*[A-Za-z])      // 영문이 있나?
            (?=.*\d)            // 숫자가 있나?
            (?=.*[!@#$...])     // 특수문자가 있나?
         */
        @Schema(description = "현재 비밀번호", example = "abc123!")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]).+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상이어야 합니다."
        )
        String currentPassword,

        @Schema(description = "수정할 비밀번호", example = "abc1234!")
        @NotBlank(message = "수정될 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>/?]).+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상이어야 합니다."
        )
        String newPassword

) {

}
