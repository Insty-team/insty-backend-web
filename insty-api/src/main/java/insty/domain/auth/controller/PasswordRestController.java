package insty.domain.auth.controller;

import insty.domain.auth.dto.request.PasswordResetMailReq;
import insty.domain.auth.dto.request.PasswordResetVerifyReq;
import insty.domain.auth.dto.request.PasswordUpdateReq;
import insty.domain.auth.dto.response.PasswordResetMailRes;
import insty.domain.auth.dto.response.PasswordResetVerifyRes;
import insty.domain.auth.dto.response.PasswordUpdateRes;
import insty.domain.auth.service.PasswordResetService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class PasswordRestController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 찾기 이메일 전송", description = "비밀번호 변경을 위해서 이메일을 통해 인증번호를 사용자에게 전송한다")
    @CustomExceptionDescription(SwaggerResponseDescription.PASSWORD_RESET_SEND_MAIL)
    @PostMapping("/password-reset/send-mail")
    public SuccessRes<PasswordResetMailRes> sendResetMail(@Valid @RequestBody PasswordResetMailReq req) {
        return SuccessRes.of(passwordResetService.sendResetMail(req.email()));
    }

    @Operation(summary = "이메일 인증코드 기반으로 인증", description = "전송된 이메일 인증코드를 기반으로 사용자를 검증한다")
    @CustomExceptionDescription(SwaggerResponseDescription.PASSWORD_RESET_VERIFY)
    @PostMapping("/password-reset/verify")
    public SuccessRes<PasswordResetVerifyRes> verifyResetCode(@Valid @RequestBody PasswordResetVerifyReq req) {
        return SuccessRes.of(passwordResetService.verifyCode(req.email(), req.code()));
    }

    @Operation(summary = "이메일 인증된 상태에서 비밀번호 변경", description = "이메일 인증코드로 인증된 사용자가 비밀번호를 변경한다")
    @CustomExceptionDescription(SwaggerResponseDescription.PASSWORD_RESET_UPDATE)
    @PostMapping("/password-reset/update")
    public SuccessRes<PasswordUpdateRes> updatePassword(@Valid @RequestBody PasswordUpdateReq req) {
        return SuccessRes.of(passwordResetService.updatePassword(req.email(), req.newPassword()));
    }
}
