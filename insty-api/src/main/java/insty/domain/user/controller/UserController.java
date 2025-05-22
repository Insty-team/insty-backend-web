package insty.domain.user.controller;


import insty.domain.user.dto.request.UserCreateReq;
import insty.domain.user.dto.request.UserEmailCheckReq;
import insty.domain.user.dto.request.UserNicknameCheckReq;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 API")
@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "이메일 회원 가입", description = "이메일로 회원 가입을 진행합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @PostMapping
    public SuccessRes<String> signUp(@Validated @RequestBody UserCreateReq req) {
        return SuccessRes.of(null);
    }

    @Operation(summary = "이메일 중복 체크", description = "이메일이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/email/check")
    public SuccessRes<String> emailCheck(@ParameterObject @Validated @ModelAttribute UserEmailCheckReq req) {
        return SuccessRes.of(null);
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임이 이미 사용중인지 중복체크를 합니다.")
    @CustomExceptionDescription(SwaggerResponseDescription.USER_INFO)
    @GetMapping("/nickname/check")
    public SuccessRes<String> nicknameCheck(@ParameterObject @Validated @ModelAttribute UserNicknameCheckReq req) {
        return SuccessRes.of(null);
    }
}


