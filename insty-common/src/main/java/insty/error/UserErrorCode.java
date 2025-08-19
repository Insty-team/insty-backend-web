package insty.error;

public enum UserErrorCode implements ErrorCode {
    USER_CURRENT_PASSWORD_NOT_MATCHED("USER_001", "현재 비밀번호와 제출한 비밀번호가 일치하지 않습니다.", 400),
    USER_PASSWORD_MISMATCH("USER_002", "비밀번호가 다릅니다.", 401),
    UNAUTHORIZED("USER_003", "사용자 인증에 실패하였습니다.", 401),
    USER_NOT_FOUND("USER_004", "사용자를 찾을 수 없습니다.", 404),
    USER_DUPLICATE_EMAIL("USER_005", "해당 이메일 정보로 가입된 계정이 존재합니다.", 409),
    USER_DUPLICATE_NICKNAME("USER_006", "이미 사용 중인 닉네임입니다.", 409),
    USER_NEW_PASSWORD_SAME_AS_CURRENT("USER_007", "현재 비밀번호와 변경하려는 비밀번호가 같습니다.", 409)

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
