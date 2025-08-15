package insty.domain.user.controller.docs;

import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserDetailRes;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "유저 API")
public interface UserControllerDocs {

    @Operation(
        summary = "내 사용자 정보 조회",
        description = "사용자가 가지고 있는 토큰 기반으로 사용자 정보를 조회합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_DETAIL)
    SuccessRes<UserDetailRes> getProfile(@CurrentUser Long userId);

    @Operation(
        summary = "내 사용자 정보 수정",
        description = "내 사용자 정보를 수정합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    SuccessRes<UserDetailRes> updateProfile(
        Long userId,
        UserUpdateReq req,
        @Parameter(description = "프로필 이미지")
        MultipartFile profileImage
    );

    @Operation(
        summary = "사용자 타입 변경",
        description = "사용자 타입을 변경합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    SuccessRes<UserDetailRes> updateUserType(Long userId, UserTypeUpdateReq req);

    @Operation(
        summary = "사용자 이메일 수신 동의 상태 값 변경",
        description = "로그아웃을 요청합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    SuccessRes<UserDetailRes> updateEmailAgreed(Long userId, UserAgreementUpdateReq req);
}
