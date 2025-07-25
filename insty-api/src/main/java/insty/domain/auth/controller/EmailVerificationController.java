package insty.domain.auth.controller;

import insty.domain.auth.controller.docs.EmailVerificationControllerDocs;
import insty.domain.auth.dto.request.EmailSendReq;
import insty.domain.auth.dto.request.EmailVerifyReq;
import insty.domain.auth.service.EmailVerificationService;
import insty.global.response.SuccessRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class EmailVerificationController implements EmailVerificationControllerDocs {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/email-verification/send")
    public SuccessRes<String> sendVerification(@Valid @RequestBody EmailSendReq req) {
        emailVerificationService.sendVerification(req.email());
        return SuccessRes.of("이메일 인증 요청에 성공했습니다.");
    }

    @PostMapping("/email-verification/verify")
    public SuccessRes<String> verifyEmailCode(@Valid @RequestBody EmailVerifyReq req) {
        emailVerificationService.verifyEmailCode(req.email(), req.code());
        return SuccessRes.of("이메일 인증에 성공했습니다.");
    }
}
