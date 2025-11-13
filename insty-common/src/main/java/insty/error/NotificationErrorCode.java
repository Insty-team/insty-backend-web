package insty.error;

public enum NotificationErrorCode implements ErrorCode {

    NOTIFICATION_MAIL_SEND_FAILED("NOTIFICATION_001", "알림 메일 발송에 실패했습니다.", 500),
    QUESTION_NOTIFICATION_FAILED("NOTIFICATION_002", "질문 알림 메일 발송에 실패했습니다.", 500),
    ANSWER_NOTIFICATION_FAILED("NOTIFICATION_003", "답변 알림 메일 발송에 실패했습니다.", 500),
    ANSWER_ACCEPT_NOTIFICATION_FAILED("NOTIFICATION_004", "답변 채택 알림 메일 발송에 실패했습니다.", 500),
    MENTION_NOTIFICATION_FAILED("NOTIFICATION_005", "멘션 알림 메일 발송에 실패했습니다.", 500),
    NOTIFICATION_CREATE_ERROR("NOTIFICATION_006", "생성 메서드 검증에 실패했습니다.", 500),
    NOTIFICATION_NOT_FOUND("NOTIFICATION_007", "존재하지 않는 알림 입니다.", 404)

    ;

    private final String code;
    private final String message;
    private final int httpCode;

    NotificationErrorCode(String code, String message, int httpCode) {
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
