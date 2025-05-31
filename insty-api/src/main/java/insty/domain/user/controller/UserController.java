package insty.domain.user.controller;


import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.service.UserService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.security.CustomUserDetails;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "유저 API")
@Validated
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "이메일 회원 가입", description = "이메일로 회원 가입을 진행합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping
    public SuccessRes<UserCreateRes> signup(@Validated @RequestBody UserCreateReq req) {
        return SuccessRes.of(userService.signup(req));
    }

    @Operation(summary = "이메일 중복 체크", description = "이메일이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/email/check")
    public SuccessRes<UserDuplicateCheckRes> emailCheck(@ParameterObject @Validated @ModelAttribute UserEmailCheckReq req) {
        return SuccessRes.of(userService.existCheckByEmail(req.email()));
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/nickname/check")
    public SuccessRes<UserDuplicateCheckRes> nicknameCheck(@ParameterObject @Validated @ModelAttribute UserNicknameCheckReq req) {
        return SuccessRes.of(userService.existsCheckByNickname(req.nickname()));
    }

    @Operation(summary = "[임시] 내 사용자 정보 조회", description = "[임시] 사용자가 가지고 있는 토큰 기반으로 사용자 정보를 조회합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/profile")
    public SuccessRes<?> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {   // TODO 커스텀 에노테이션으로 인증 유저 편한 값으로 변경
        return SuccessRes.of(userDetails);
    }
}


