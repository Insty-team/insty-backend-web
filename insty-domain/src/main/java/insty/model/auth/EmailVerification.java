package insty.model.auth;

import static insty.error.AuthErrorCode.ALREADY_VERIFIES_EMAIL;
import static insty.error.AuthErrorCode.INVALID_EMAIL_FORMAT;
import static insty.error.AuthErrorCode.INVALID_TOKEN_CODE;

import insty.exception.CustomException;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "email")
public class EmailVerification {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);
    private static final int TOKEN_LENGTH = 6;

    private final String email;
    private String token;
    private boolean verified;

    private EmailVerification(String email, String token) {
        if (email == null || email.isBlank()) {
            throw new CustomException(INVALID_EMAIL_FORMAT);
        }
        if (!PATTERN.matcher(email).matches()) {
            throw new CustomException(INVALID_EMAIL_FORMAT);
        }
        this.email = email;
        this.token = token;
        this.verified = false;
    }

    public static EmailVerification create(String email, TokenGenerator tokenGenerator) {
        String token = tokenGenerator.generate(TOKEN_LENGTH);
        return new EmailVerification(email, token);
    }

    public EmailVerification reissue(TokenGenerator tokenGenerator) {
        String newToken = tokenGenerator.generate(TOKEN_LENGTH);
        return new EmailVerification(this.email, newToken);
    }

    public void verify(String inputToken) {
        if (this.verified) {
            throw new CustomException(ALREADY_VERIFIES_EMAIL);
        }
        if (!token.equals(inputToken)) {
            throw new CustomException(INVALID_TOKEN_CODE);
        }
        verified = true;
    }
}