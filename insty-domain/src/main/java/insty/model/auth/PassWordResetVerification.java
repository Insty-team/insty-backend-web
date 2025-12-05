package insty.model.auth;

import insty.error.AuthErrorCode;
import insty.exception.CustomException;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static insty.error.AuthErrorCode.INVALID_EMAIL_FORMAT;

@Getter
public class PassWordResetVerification {

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);
    private static final int TOKEN_LENGTH = 6;

    private String email;
    private String code;
    private boolean verified;
    private final LocalDateTime expiredAt;

    private PassWordResetVerification(String email,String code,LocalDateTime expiredAt){
        this.email = email;
        this.code = code;
        this.expiredAt = expiredAt;
        this.verified = false;
    }

    public static PassWordResetVerification create(String email,TokenGenerator generator){

        if (email == null || email.isBlank() || !PATTERN.matcher(email).matches()) {
            throw new CustomException(INVALID_EMAIL_FORMAT);
        }

        return new PassWordResetVerification(
                email,
                generator.generate(TOKEN_LENGTH),
                LocalDateTime.now().plusMinutes(5));
    }

    public PassWordResetVerification reissue(TokenGenerator generator){
        return new PassWordResetVerification(
                this.email,
                generator.generate(TOKEN_LENGTH),
                LocalDateTime.now().plusMinutes(5)
        );
    }

    public void verify(String inputCode){
        if(this.verified){
            throw new CustomException(AuthErrorCode.ALREADY_VERIFIES_EMAIL);
        }

        if(!this.code.equals(inputCode)){
            throw new CustomException(AuthErrorCode.INVALID_TOKEN_CODE);
        }
        this.verified = true;
    }


}
