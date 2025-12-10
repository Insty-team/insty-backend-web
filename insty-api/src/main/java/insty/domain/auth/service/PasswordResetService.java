package insty.domain.auth.service;

import insty.domain.auth.dto.response.PasswordResetMailRes;
import insty.domain.auth.dto.response.PasswordResetVerifyRes;
import insty.domain.auth.dto.response.PasswordUpdateRes;
import insty.domain.auth.implement.emailverification.MailAuthenticateContent;
import insty.domain.auth.implement.passwordreset.PasswordResetReader;
import insty.domain.auth.implement.passwordreset.PasswordResetWriter;
import insty.domain.user.implement.UserReader;
import insty.domain.user.implement.UserValidator;
import insty.error.AuthErrorCode;
import insty.error.UserErrorCode;
import insty.exception.CustomException;
import insty.mail.MailHelper;
import insty.model.auth.PasswordResetVerification;
import insty.model.auth.TokenGenerator;
import insty.model.user.User;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
@Slf4j
public class PasswordResetService {

    private final PasswordResetWriter passwordResetWriter;
    private final PasswordResetReader passwordResetReader;
    private final MailHelper mailHelper;
    private final UserValidator userValidator;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserReader userReader;
    private final TokenGenerator tokenGenerator;

    public PasswordResetMailRes sendResetMail(@NotNull String email){
        if(userValidator.existsEmail(email)){
            PasswordResetVerification passwordResetVerification = passwordResetReader.findOptionalByEmail(email)
                    .map(verification -> verification.reissue(tokenGenerator))
                    .orElse(PasswordResetVerification.create(email, tokenGenerator));

            MailAuthenticateContent mailContent = MailAuthenticateContent.of(email, passwordResetVerification.getCode());
            mailHelper.send(mailContent);
            passwordResetWriter.save(passwordResetVerification);
            return PasswordResetMailRes.from(email,passwordResetVerification.getExpiredAt());
        }else{
            // 존재하지 않는 email도 동일한 성공 응답 처리 ( 계정 존재 여부 숨기기 위함 )
            log.debug("사용자 이메일 [{}]가 존재하지 않아 메일 발송을 생략합니다.", email);
            return PasswordResetMailRes.from(email,LocalDateTime.now());
        }
    }

    public PasswordResetVerifyRes verifyCode(@NotNull String email, @NotNull String code){
        PasswordResetVerification verification = passwordResetReader.findByEmail(email);
        verification.verify(code);
        passwordResetWriter.save(verification);
        return PasswordResetVerifyRes.from(email,verification.isVerified(),verification.getExpiredAt());
    }

    public PasswordUpdateRes updatePassword(@NotNull String email, @NotNull String newPassword) {

        if (!passwordResetReader.isVerified(email)) {
            throw new CustomException(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }
        User userByEmail = userReader.getUserByEmail(email);
        userByEmail.changePassword(passwordEncoder.encode(newPassword));
        return PasswordUpdateRes.from(email,true, LocalDateTime.now());
    }


}
