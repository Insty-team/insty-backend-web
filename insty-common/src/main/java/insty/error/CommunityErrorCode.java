package insty.error;

public enum CommunityErrorCode implements ErrorCode {

    COMMUNITY_QUESTION_NOT_FOUND("COMMUNITY_001", "Community question not found", 404),
    COMMUNITY_ANSWER_NOT_FOUND("COMMUNITY_002", "Community answer not found", 404),
    COMMUNITY_CREATE_ERROR("COMMUNITY_004", "Community craete failed.", 500),
    COMMUNITY_INVALID_VIDEO_UUID("COMMUNITY_005", "Invalid video UUID format", 400),
    COMMUNITY_ANSWER_ACCEPT_PERMISSION_DENIED("COMMUNITY_006", "Only question author can accept answers", 403),
    COMMUNITY_ANSWER_NOT_BELONG_TO_QUESTION("COMMUNITY_007", "Answer does not belong to the specified question", 400),
    COMMUNITY_UPDATE_ERROR("COMMUNITY_008", "Community update failed", 500),
    COMMUNITY_DELETE_ERROR("COMMUNITY_009", "Community delete failed", 500)
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
