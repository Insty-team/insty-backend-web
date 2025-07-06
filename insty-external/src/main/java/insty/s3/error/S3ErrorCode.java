package insty.s3.error;

import insty.error.ErrorCode;

public enum S3ErrorCode implements ErrorCode {
    S3_UPLOAD_ERROR("S3_001", "S3 파일 업로드에 실패했습니다.", 500),
    S3_HEAD_ERROR("S3_002", "S3 HEAD 요청에 실패했습니다.", 500),

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
