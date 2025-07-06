package insty.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateReq (

        @Schema(description = "이메일 주소", example = "youremail@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @Schema(description = "닉네임", example = "유재석")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣0-9]{2,10}$", message = "닉네임은 한글 또는 영문만 허용되며 특수문자는 사용할 수 없습니다.")
        String nickname,

        @Schema(description = "소개글", example = "안녕하세요 저를 소개합니다.")
        @Size(max = 4000, message = "소개글은 4000자 이하로 입력 가능합니다.")
        String introduce
) {

}
