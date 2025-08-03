package insty.error;

public enum AuthErrorCode implements ErrorCode {
    INVALID_EMAIL_FORMAT("AUTH_001", "유효하지 않은 이메일 형식입니다.", 400),
    ALREADY_VERIFIES_EMAIL("AUTH_002", "이미 인증된 이메일입니다.", 401), 
    REQUIRES_EMAIL_VERIFICATION_REQUEST("AUTH_003", "이메일 인증 요청을 해주세요.", 400),
    INVALID_TOKEN_CODE("AUTH_004", "잘못된 인증코드 입니다.", 401);

    private final String code;
    private final String message;
    private final int httpCode;

    AuthErrorCode(String code, String message, int httpCode) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpCode() {
        return httpCode;
    }
}
