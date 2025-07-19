package insty.ai.error;

import insty.error.ErrorCode;

public enum AiErrorCode implements ErrorCode {
    AI_API_REQUEST_FAILED("AI_001", "AI 서버와의 통신에 실패했습니다.", 500),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    AiErrorCode(String code, String message, int httpCode) {
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
