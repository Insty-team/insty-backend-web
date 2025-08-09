package insty.domain.user.controller.docs;

import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserPasswordUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "유저 API")
public interface AccountControllerDocs {

    @Operation(summary = "이메일 회원 가입", description = "이메일로 회원 가입을 진행합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_CREATE)
    SuccessRes<UserCreateRes> signup(UserCreateReq req);

    @Operation(summary = "이메일 중복 체크", description = "이메일이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    SuccessRes<Void> emailCheck(@ParameterObject UserEmailCheckReq req);

    @Operation(summary = "닉네임 중복 체크", description = "닉네임이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    SuccessRes<Void> nicknameCheck(@ParameterObject UserNicknameCheckReq req);

    @Operation(
        summary = "내 비밀번호 수정 수정",
        description = "내 비밀번호를 수정합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    SuccessRes<UserDetailRes> updatePassword(Long userId, UserPasswordUpdateReq req);
}
