package insty.error;

public enum CourseErrorCode implements ErrorCode {
    COURSE_NOT_FOUND("COURSE_001", "강의를 찾을 수 없습니다.", 404),
    COURSE_TOO_MANY_PRACTICE_FILE("COURSE_002", "허용된 실습파일 개수를 초과했습니다.", 413),
    COURSE_CANT_CHANGE("COURSE_003", "강의 생성자가 아닙니다.", 403),
    COURSE_CREATE_ERROR("COURSE_004", "생성 메서드 검증에 실패했습니다.", 500),
    COURSE_THUMBNAIL_INVALID_EXTENSION("COURSE_005", "썸네일은 jpg, jpeg, png만 업로드 가능합니다.", 400),
    COURSE_NOT_FOUND_LINKED_VIDEO("COURSE_006", "강의에 연결된 영상이 없습니다.", 404),

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
