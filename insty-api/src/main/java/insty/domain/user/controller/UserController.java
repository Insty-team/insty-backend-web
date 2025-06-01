package insty.domain.user.controller;


import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserLoginReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.domain.user.dto.response.UserCreateRes;
import insty.domain.user.dto.response.UserDuplicateCheckRes;
import insty.domain.user.dto.response.UserLoginRes;
import insty.domain.user.service.UserService;
import insty.error.CommonErrorCode;
import insty.exception.CustomException;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.security.CustomUserDetails;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "로그인 (Swagger 문서용)", description = "이메일과 비밀번호로 로그인합니다. | 실제 인증은 Spring Security Filter에서 처리됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserLoginRes.class),
                                    examples = @ExampleObject(value = """
                                            {
                                                 "success": true,
                                                 "data": {
                                                     "id": 1,
                                                     "nickname": "test@example.com",
                                                     "userType": "NONE",
                                                     "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwicm9sZSI6Ik5PTkUiLCJpYXQiOjE3NDg3NDMzMDQsImV4cCI6MjA2NDEwMzMwNH0.TNM4Dh5ZTNemn3aKrtMCMN7JT_YMZx80nlLSectikkNoYfeI-5KiFJz6HjJfKqTdnOrI4xEo_kOt_3cdQMtoNA",
                                                     "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwianRpIjoiZTg3MWNiNDMtYWFjMC00NDU1LWFhOTUtMjQ0Y2IyZmIzMTM5IiwiaWF0IjoxNzQ4NzQzMzA0LCJleHAiOjIwNjQxMDMzMDR9.5KL9PHdKItQKEVE9Dep3k3YQiCNCxNAv4HEACYKFGbu1BKIJPl8oY-sldJnDAgPfEs5DofgNHkCJ7bRTlsTPcw",
                                                     "accessTokenExpiresIn": 2064103304000,
                                                     "refreshTokenExpiresIn": 2064103304000
                                                 }
                                            }
                                    """)
                            )
                    )
            }
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping("/login")
    public SuccessRes<UserLoginRes> login(@RequestBody UserLoginReq userLoginReq) {
        throw new CustomException(CommonErrorCode.DOCUMENTATION_ONLY);
    }
}


