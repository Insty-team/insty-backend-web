package insty.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserNicknameCheckReq(
        @Schema(description = "닉네임", example = "유재석")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10, message = "닉네임은 최대 10자까지 가능합니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,10}$", message = "닉네임은 한글 또는 영문만 허용되며 특수문자는 사용할 수 없습니다.")
        String nickname
) {

}
