package insty.model.auth;

import java.security.SecureRandom;
import lombok.Getter;

@Getter
public class SimpleTokenGenerator implements TokenGenerator {

    private static final String DIGITS = "0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generate(int tokenLength) {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < tokenLength; i++) {
            token.append(DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length())));
        }
        return token.toString();
    }
}