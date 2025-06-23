package insty.error;

public enum ExampleErrorCode implements ErrorCode {
    EXAMPLE_ERROR_CODE("EXAMPLE_001", "예시 오류메시지입니다.", 401);

    private final String code;
    private final String message;
    private final int httpCode;

    ExampleErrorCode(String code, String message, int httpCode) {
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
