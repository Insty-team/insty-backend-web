package insty.model.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.AuthErrorCode;
import insty.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailVerificationTest {
    
    @Test
    void 이메일_정보로_6자리_토큰을_가진_인증되지_않은_이메일_인증_정보가_생성된다() {
        // given
        String email = "test@insty.com";
        String token = "givenSixLength";
        TokenGenerator tokenGenerator = length -> token;

        // when
        EmailVerification emailVerification = EmailVerification.create(email, tokenGenerator);

        // then
        assertThat(emailVerification.getEmail()).isEqualTo(email);
        assertThat(emailVerification.getToken()).isEqualTo(token);
        assertThat(emailVerification.isVerified()).isFalse();
    }

    @NullAndEmptySource
    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",          // @ 없음
        "@domain.com",            // 호스트 없음
        "user@",                  // 도메인 없음
        "user@domain",            // TLD 없음
        "user@.com",              // 도메인명 없음
        "user@domain.c",          // TLD 짧음 (1자리)
        "user@domain.toolong",    // TLD 김 (7자리)
        "user name@domain.com",   // 공백 포함
        "user@domain .com",       // 도메인 공백
        "user@@domain.com",       // @ 중복
    })
    void 유효하지_않은_이메일_형식들은_예외를_발생시킨다(String invalidEmail) {
        // given
        TokenGenerator tokenGenerator = length -> "123456";

        // when & then
        assertThatThrownBy(() -> EmailVerification.create(invalidEmail, tokenGenerator))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_EMAIL_FORMAT);
    }

    @Test
    void 이메일_인증_정보_재발급시_토큰이_변경되고_인증되지_않은_상태가_된다() {
        // given
        EmailVerification emailVerification = EmailVerification.create("test@insty.com", length -> "prevToken");
        String prevToken = emailVerification.getToken();
        emailVerification.verify();

        // when
        emailVerification.reissue(length -> "newToken");
        
        // then
        assertThat(emailVerification.getToken()).isNotEqualTo(prevToken);
        assertThat(emailVerification.isVerified()).isFalse();
    }
    
    @Test
    void 유효한_토큰으로_비교_시_true를_반환한다() {
        // given
        EmailVerification emailVerification = EmailVerification.create("test@insty.com", length -> "validToken");
        
        // when
        boolean result = emailVerification.hasSameToken("validToken");

        // then
        assertThat(result).isTrue();
    }
    
    @Test
    void 유효하지_않은_토큰으로_비교_시_false를_반환한다() {
        // given
        EmailVerification emailVerification = EmailVerification.create("test@insty.com", length -> "validToken");
        
        // when
        boolean result = emailVerification.hasSameToken("invalidToken");
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    void 이미_검증된_토큰일_경우_예외가_발생한다() {
        // given
        EmailVerification emailVerification = EmailVerification.create("test@insty.com", length -> "validToken");
        emailVerification.verify();
        
        // when & then
        assertThatThrownBy(() -> emailVerification.hasSameToken("validToken"))
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.ALREADY_VERIFIES_EMAIL);
    }
}