package insty.error;

public enum CommunityErrorCode implements ErrorCode {

    COMMUNITY_QUESTION_NOT_FOUND("Community_001", "Community question not found", 404),
    COMMUNITY_ANSWER_NOT_FOUND("Community_002", "Community answer not found", 404)
    ;

    private final String code;
    private final String message;
    private final int httpCode;

    CommunityErrorCode(String code, String message, int httpCode) {
        this.code = code;
        this.message = message;
        this.httpCode = httpCode; // Default HTTP code for not found
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
