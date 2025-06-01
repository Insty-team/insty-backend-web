package insty.error;

public enum UserErrorCode implements ErrorCode {
    USER_PASSWORD_MISMATCH("USER_001", "비밀번호가 다릅니다.", 401),
    UNAUTHORIZED("USER_002", "사용자 인증에 실패하였습니다.", 401),
    USER_NOT_FOUND("USER_003", "사용자를 찾을 수 없습니다.", 404),
    USER_DUPLICATE_EMAIL("USER_004", "이미 사용 중인 이메일입니다.", 409),
    USER_DUPLICATE_NICKNAME("USER_005", "이미 사용 중인 닉네임입니다.", 409)

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
