package insty.domain.user.controller;


import insty.domain.user.dto.request.UserAgreementUpdateReq;
import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.request.UserTypeUpdateReq;
import insty.domain.user.dto.request.UserUpdateReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDetailRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.service.UserService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "유저 API")
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "이메일 회원 가입", description = "이메일로 회원 가입을 진행합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_CREATE)
    @PostMapping
    public SuccessRes<UserCreateRes> signup(@Validated @RequestBody UserCreateReq req) {
        return SuccessRes.of(userService.signup(req));
    }

    @Operation(summary = "이메일 중복 체크", description = "이메일이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/email/check")
    public SuccessRes<UserDuplicateCheckRes> emailCheck(@ParameterObject @Validated @ModelAttribute UserEmailCheckReq req) {
        return SuccessRes.of(userService.existCheckByEmail(req));
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/nickname/check")
    public SuccessRes<UserDuplicateCheckRes> nicknameCheck(@ParameterObject @Validated @ModelAttribute UserNicknameCheckReq req) {
        return SuccessRes.of(userService.existsCheckByNickname(req));
    }

    @Operation(
            summary = "내 사용자 정보 조회",
            description = "사용자가 가지고 있는 토큰 기반으로 사용자 정보를 조회합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_DETAIL)
    @GetMapping("/profile")
    public SuccessRes<UserDetailRes> getProfile(@CurrentUser Long userId) {
        return SuccessRes.of(userService.getDetailUser(userId));
    }


    @Operation(
            summary = "내 사용자 정보 수정",
            description = "내 사용자 정보를 수정합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PutMapping(value = "/profile/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<UserDetailRes> updateProfile(
            @CurrentUser Long userId,
            @RequestPart("userUpdateReq") @Validated UserUpdateReq req,
            @Parameter(description = "프로필 이미지", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        return SuccessRes.of(userService.updateUser(userId, req, profileImage));
    }

    @Operation(
            summary = "사용자 타입 변경",
            description = "사용자 타입을 변경합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    @PatchMapping("/profile/userType")
    public SuccessRes<UserDetailRes> updateUserType(
            @CurrentUser Long userId,
            @Validated @RequestBody UserTypeUpdateReq req) {
        return SuccessRes.of(userService.updateUserType(userId, req));
    }

    @Operation(
            summary = "사용자 이메일 수신 동의 상태 값 변경",
            description = "로그아웃을 요청합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_UPDATE)
    @PatchMapping("/profile/email-agree")
    public SuccessRes<UserDetailRes> updateEmailAgreed(
            @CurrentUser Long userId,
            @RequestBody UserAgreementUpdateReq req) {
        return SuccessRes.of(userService.updateAgreement(userId, req));
    }

}


