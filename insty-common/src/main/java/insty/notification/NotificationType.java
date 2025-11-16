package insty.notification;


public enum NotificationType {
    INFO("알림", "notification", null, false),
    NEW_COURSE("새 강의", "new-course", null, true),
    NEW_COMMUNITY_QUESTION("새 질문", "community-question", "[INSTY] 새로운 질문이 등록되었습니다", true),
    NEW_COMMUNITY_ANSWER("새 답변", "community-answer", "[INSTY] 새로운 답변이 달렸습니다", true),
    COMMUNITY_ANSWER_ACCEPT("답변 채택", "answer-accept", "[INSTY] 답변이 채택되었습니다", true),
    USER_MENTIONED("멘션", "user-mention", "[INSTY] 누군가 당신을 언급했습니다", true);

    private final String displayName;
    private final String templateName;
    private final String emailSubject;
    private final boolean userConfigurable;  // 사용자가 설정 가능한지 여부

    NotificationType(String displayName, String templateName, String emailSubject, boolean userConfigurable) {
        this.displayName = displayName;
        this.templateName = templateName;
        this.emailSubject = emailSubject;
        this.userConfigurable = userConfigurable;
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

    /**
     * 사용자가 설정 가능한 알림 타입인지 확인
     */
    public boolean isUserConfigurable() {
        return userConfigurable;
    }

    /**
     * 사용자가 설정 가능한 알림 타입만 반환
     */
    public static NotificationType[] getUserConfigurableTypes() {
        return java.util.Arrays.stream(values())
                .filter(NotificationType::isUserConfigurable)
                .toArray(NotificationType[]::new);
    }
}
