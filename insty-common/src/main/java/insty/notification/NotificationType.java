package insty.notification;

/**
 * 알림 타입 열거형
 * 메타데이터를 포함하여 확장성을 높임
 */
public enum NotificationType {
    INFO("알림", "notification", null),
    NEW_COURSE("새 강의", "new-course", null),
    NEW_COMMUNITY_QUESTION("새 질문", "community-question", "[INSTY] 새로운 질문이 등록되었습니다"),
    NEW_COMMUNITY_ANSWER("새 답변", "community-answer", "[INSTY] 새로운 답변이 달렸습니다"),
    COMMUNITY_ANSWER_ACCEPT("답변 채택", "answer-accept", "[INSTY] 답변이 채택되었습니다"),
    USER_MENTIONED("멘션", "user-mention", "[INSTY] 누군가 당신을 언급했습니다");

    private final String displayName;
    private final String templateName;
    private final String emailSubject;

    NotificationType(String displayName, String templateName, String emailSubject) {
        this.displayName = displayName;
        this.templateName = templateName;
        this.emailSubject = emailSubject;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getEmailSubject() {
        return emailSubject != null ? emailSubject : "[INSTY] " + displayName;
    }
}
