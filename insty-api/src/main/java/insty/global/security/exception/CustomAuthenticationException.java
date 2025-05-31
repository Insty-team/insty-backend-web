package insty.global.security.exception;

import insty.error.CommonErrorCode;
import insty.error.ErrorCode;
import insty.error.UserErrorCode;
import org.springframework.security.core.AuthenticationException;

public class CustomAuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public CustomAuthenticationException(CommonErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomAuthenticationException(UserErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
