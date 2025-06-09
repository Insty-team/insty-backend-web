package insty.error;

public enum CourseErrorCode implements ErrorCode {
    COURSE_NOT_FOUND("COURSE_001", "강의를 찾을 수 없습니다.", 404),
    COURSE_TOO_MANY_PRACTICE_FILE("COURSE_002", "허용된 실습파일 개수를 초과했습니다.", 413),
    COURSE_CANT_DELETE("COURSE_003", "강의 생성자가 아닙니다.", 403),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    CourseErrorCode(String code, String message, int httpCode) {
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
