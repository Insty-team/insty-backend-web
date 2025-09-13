package insty.error;

public enum CourseProgressErrorCode implements ErrorCode{
    COURSE_PROGRESS_DUPLICATE("COURSE_PROGRESS_001","이미 수강신청된 강의입니다.",409),
    COURSE_PROGRESS_CREATE_ERROR("COURSE_PROGRESS_002","생성자 메서드 검증 실패",500);

    private final String code;
    private final String message;
    private final int httpCode;

    CourseProgressErrorCode(String code, String message, int httpCode) {
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
