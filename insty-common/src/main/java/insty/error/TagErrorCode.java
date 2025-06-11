package insty.error;

public enum TagErrorCode implements ErrorCode {
    TAG_CREATE_ERROR("TAG_001", "생성 메서드 검증에 실패했습니다.", 500),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    TagErrorCode(String code, String message, int httpCode) {
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
