package insty.error;

public enum CategoryErrorCode implements ErrorCode {
    CATEGORY_INVALID_DEPTH("CATEGORY_001", "카테고리의 깊이가 유효하지 않습니다.", 400),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    CategoryErrorCode(String code, String message, int httpCode) {
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
