package insty.error;

public enum VideoErrorCode implements ErrorCode {
    VIDEO_TYPE_NOT_MATCH("VIDEO_001", "파일 확장자와 영상 형식이 일치하지 않습니다.", 400),
    VIDEO_CONTENT_TYPE_ERROR("VIDEO_002", "지원하지 않는 영상 형식입니다.", 400),
    VIDEO_INVALID_FILE_NAME("VIDEO_003", "파일 이름이 유효하지 않습니다.", 400),
    VIDEO_EXCEED_UPLOAD_LIMIT("VIDEO_004", "하루 업로드 제한량을 초과했습니다.", 403),
    VIDEO_CANT_READ("VIDEO_005", "동영상을 재생할 권한이 없습니다.", 403),
    VIDEO_NOT_FOUND("VIDEO_006", "존재하지 않는 영상입니다.", 404),
    VIDEO_NOT_FINISHED_ENCODING("VIDEO_007", "인코딩이 완료되지 않았습니다.", 409),
    VIDEO_INVALID_ENCODING_KEY("VIDEO_008", "인코딩 키의 값이 유효하지 않습니다.", 500),
    VIDEO_CREATE_ERROR("VIDEO_009", "생성 메서드 검증에 실패했습니다.", 500),

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
