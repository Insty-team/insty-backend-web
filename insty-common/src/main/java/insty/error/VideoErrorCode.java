package insty.error;

public enum VideoErrorCode implements ErrorCode {
    VIDEO_TYPE_NOT_MATCH("VIDEO_001", "파일 확장자와 영상 형식이 일치하지 않습니다.", 400),
    VIDEO_CONTENT_TYPE_ERROR("VIDEO_002", "지원하지 않는 영상 형식입니다.", 400),
    VIDEO_INVALID_FILE_NAME("VIDEO_003", "파일 이름이 유효하지 않습니다.", 400),
    VIDEO_NOT_FOUND("VIDEO_004", "존재하지 않는 영상입니다.", 404),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    VideoErrorCode(String code, String message, int httpCode) {
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
