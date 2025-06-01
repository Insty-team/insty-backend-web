package insty.error;

public enum TokenErrorCode implements ErrorCode {

    /*
     * 400 BAD REQUEST
     */
    TOKEN_MALFORMED("TOKEN_001", "토큰 형식이 잘못되었습니다.", 400),
    TOKEN_UNSUPPORTED("TOKEN_002", "지원하지 않는 토큰입니다.", 400),
    TOKEN_CLAIMS_INVALID("TOKEN_003", "토큰의 클레임이 유효하지 않습니다.", 400),

    /*
     * 401 UNAUTHORIZED
     */
    ACCESS_TOKEN_MISSING("TOKEN_004", "액세스 토큰이 존재하지 않습니다.", 401),
    ACCESS_TOKEN_INVALID("TOKEN_005", "유효하지 않은 액세스 토큰입니다.", 401),
    ACCESS_TOKEN_EXPIRED("TOKEN_006", "액세스 토큰이 만료되었습니다.", 401),
    ACCESS_TOKEN_SIGNATURE_INVALID("TOKEN_007", "액세스 토큰의 서명이 올바르지 않습니다.", 401),
    ACCESS_TOKEN_USER_MISMATCH("TOKEN_008", "액세스 토큰의 사용자 정보가 일치하지 않습니다.", 401),

    REFRESH_TOKEN_MISSING("TOKEN_009", "리프레시 토큰이 존재하지 않습니다.", 401),
    REFRESH_TOKEN_INVALID("TOKEN_010", "유효하지 않은 리프레시 토큰입니다.", 401),
    REFRESH_TOKEN_EXPIRED("TOKEN_011", "리프레시 토큰이 만료되었습니다.", 401),
    REFRESH_TOKEN_SIGNATURE_INVALID("TOKEN_012", "리프레시 토큰의 서명이 올바르지 않습니다.", 401),
    REFRESH_TOKEN_USER_MISMATCH("TOKEN_013", "리프레시 토큰의 사용자 정보가 일치하지 않습니다.", 401),

    /*
     * 404 NOT FOUND
     */
    REFRESH_TOKEN_NOT_FOUND("TOKEN_014", "리프레시 토큰이 존재하지 않습니다.", 404),

    /*
     * 500 INTERNAL SERVER ERROR
     */
    UNKNOWN_TOKEN_ERROR("TOKEN_999", "알 수 없는 토큰 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int httpCode;

    TokenErrorCode(String code, String message, int httpCode) {
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
