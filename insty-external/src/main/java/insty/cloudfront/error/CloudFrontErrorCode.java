package insty.cloudfront.error;

import insty.error.ErrorCode;

public enum CloudFrontErrorCode implements ErrorCode {
    CLOUD_FRONT_GENERATE_SIGNED_URL_FAIL("CLOUD_FRONT_001", "CloudFront의 서명 URL 생성에 실패했습니다.", 500),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    CloudFrontErrorCode(String code, String message, int httpCode) {
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
