package insty.model.mention;

public enum MentionTargetType {
    COURSE_QUESTION("QUESTION"),
    COURSE_ANSWER("ANSWER"),
    COMMUNITY_POST("COMMUNITY_POST"),
    COMMUNITY_COMMENT("COMMUNITY_COMMENT");

    private final String notificationContentType;

    MentionTargetType(String notificationContentType) {
        this.notificationContentType = notificationContentType;
    }

    public String toNotificationContentType() {
        return notificationContentType;
    }
}
