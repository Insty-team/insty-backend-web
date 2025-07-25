package insty.domain.auth.controller.docs;

import insty.domain.auth.dto.request.EmailSendReq;
import insty.domain.auth.dto.request.EmailVerifyReq;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증 및 토큰 API")
public interface EmailVerificationControllerDocs {

    @Operation(summary = "이메일 인증", description = "요청한 메일로 이메일 인증 번호가 전달됩니다.")
    SuccessRes<Void> sendVerification(EmailSendReq req);

    @Operation(summary = "이메일 인증 확인", description = "이메일과 인증코드로 확인을 합니다.")
    SuccessRes<Void> verifyEmailCode(EmailVerifyReq req);
}
