package insty.s3.error;

import insty.error.ErrorCode;

public enum S3ErrorCode implements ErrorCode {
    S3_FETCH_FILE_ERROR("S3_001", "S3로부터 파일 가져오기에 실패했습니다.", 500),

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    S3ErrorCode(String code, String message, int httpCode) {
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
