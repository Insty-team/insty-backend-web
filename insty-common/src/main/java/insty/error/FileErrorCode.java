package insty.error;

public enum FileErrorCode implements ErrorCode {
    FILE_NOT_FOUND("FILE_001", "존재하지 않는 파일입니다.", 404),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    FileErrorCode(String code, String message, int httpCode) {
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
