package insty.error;

public enum UserErrorCode implements ErrorCode {
    USER_DUPLICATE_EMAIL("USER_001", "이미 사용 중인 이메일입니다.", 409),
    USER_DUPLICATE_NICKNAME("USER_002", "이미 사용 중인 닉네임입니다.", 409),
    USER_NOT_FOUND("USER_003", "사용자를 찾을 수 없습니다.", 404),
    USER_PASSWORD_MISMATCH("USER_004", "비밀번호가 다릅니다.", 401)

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    UserErrorCode(String code, String message, int httpCode) {
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
