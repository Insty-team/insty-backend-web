package insty.domain.user.dto.request;

public record UserNotificationPreferenceUpdateReq(
        boolean userMentionNotificationEnabled,
        boolean userMentionEmailEnabled,
        boolean newQuestionNotificationEnabled,
        boolean newQuestionEmailEnabled,
        boolean newAnswerNotificationEnabled,
        boolean newAnswerEmailEnabled,
        boolean answerAcceptedNotificationEnabled,
        boolean answerAcceptedEmailEnabled
) {
}