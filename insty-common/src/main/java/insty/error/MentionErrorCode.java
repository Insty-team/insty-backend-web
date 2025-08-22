package insty.error;

public enum MentionErrorCode implements ErrorCode {
    MENTION_CREATE_ERROR("MENTION_001", "멘션 생성에 실패했습니다.", 500),
    MENTION_SELF_ERROR("MENTION_002", "자기 자신을 멘션할 수 없습니다.", 400),
    MENTION_USER_NOT_FOUND("MENTION_003", "멘션된 사용자를 찾을 수 없습니다.", 404),
    MENTION_INVALID_FORMAT("MENTION_004", "멘션 형식이 올바르지 않습니다.", 400),
    MENTION_LIMIT_EXCEEDED("MENTION_005", "멘션 가능한 사용자 수를 초과했습니다.", 400),
    MENTION_COOLDOWN_VIOLATION("MENTION_006", "멘션 쿨다운 시간이 지나지 않았습니다.", 429);

    private final String code;
    private final String message;
    private final int httpCode;

    MentionErrorCode(String code, String message, int httpCode) {
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
