package insty.domain.auth.controller;

import insty.domain.auth.service.AuthService;
import insty.domain.user.dto.request.UserLoginReq;
import insty.domain.auth.dto.response.AuthUserRes;
import insty.domain.user.dto.request.UserSocialLoginReq;
import insty.error.TokenErrorCode;
import insty.exception.CustomException;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import insty.model.user.SocialType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "인증 및 토큰 API")
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "사용자 이메일 로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping("/login")
    public SuccessRes<AuthUserRes> login(@RequestBody UserLoginReq req) {
        return SuccessRes.of(authService.loginByEmail(req));
    }


    @Operation(summary = "사용자 소셜 로그인(카카오, 네이버, 구글)", description = "소셜 인증으로 로그인합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping("/login/{socialName}")
    public SuccessRes<AuthUserRes> loginWithSocial(
            @PathVariable SocialType socialName,
            @RequestBody UserSocialLoginReq req
    ) {
        return SuccessRes.of(authService.loginBySocial(socialName, req));
    }

    @Operation(
            summary = "AccessToken 재발급",
            description = "RefreshToken을 이용해 AccessToken을 재발급받습니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping("/reissue")
    public SuccessRes<AuthUserRes> reissue(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(TokenErrorCode.REFRESH_TOKEN_MISSING);
        }

        String refreshToken = authorization.substring(7); // "Bearer " 제거
        return SuccessRes.of(authService.reissueByRefreshToken(refreshToken));
    }

    @Operation(
            summary = "사용자 로그아웃",
            description = "로그아웃을 요청합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping("/logout")
    public SuccessRes<Void> logout(@CurrentUser Long userId) {
        authService.logout(userId);
        return SuccessRes.of();
    }
}
