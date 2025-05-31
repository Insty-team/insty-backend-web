package insty.error;

public enum CourseErrorCode implements ErrorCode {
    COURSE_NOT_FOUND("COURSE_001", "강의를 찾을 수 없습니다.", 404),

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
