package insty.error;

public enum SocialErrorCode implements ErrorCode {
    SOCIAL_UNSUPPORTED_TYPE("SOCIAL_001", "지원하지 않는 소셜 로그인 타입입니다.", 400),
    CLIENT_ERROR("SOCIAL_002", "요청하신 내용에 문제가 있습니다. 입력하신 정보를 다시 한 번 확인해 주세요.", 400),
    NOT_FOUND_PASSWORD("SOCIAL_003", "소셜 로그인 회원은 비밀번호를 변경하실 수 없습니다.", 403),
    TEMPORARY_SERVER_ERROR("SOCIAL_004", "현재 서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.", 500),
    MESSAGE_CONVERSION_ERROR("SOCIAL_005", "서버에서 받은 데이터를 처리하는 중 문제가 발생했습니다.", 500),
    UNKNOWN_ERROR("SOCIAL_006", "소셜 로그인 중 알 수 없는 오류가 발생했습니다.", 500);

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    SocialErrorCode(String code, String message, int httpCode) {
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
