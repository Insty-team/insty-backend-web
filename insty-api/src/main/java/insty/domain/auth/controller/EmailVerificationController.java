package insty.domain.auth.controller;

import insty.domain.auth.controller.docs.EmailVerificationControllerDocs;
import insty.domain.auth.dto.request.EmailSendReq;
import insty.domain.auth.dto.request.EmailVerifyReq;
import insty.domain.auth.service.EmailVerificationService;
import insty.global.response.SuccessRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public SuccessRes<Void> sendEmailVerification(@Valid @RequestBody EmailSendReq req) {
        emailVerificationService.sendVerification(req.email());
        return SuccessRes.of();
    }

    @PostMapping("/email-verification/verify")
    public SuccessRes<Void> verifyEmailVerification(@Valid @RequestBody EmailVerifyReq req) {
        return SuccessRes.of();
    }
}
