package insty.notification;

import java.util.HashMap;
import java.util.Map;

/**
 * 알림 요청 DTO
 * 외부 모듈에서 알림 시스템으로 전달되는 통합 이벤트
 * 모든 알림 타입에 대해 이 하나의 클래스만 사용
 */
public record NotificationRequest(
        NotificationType type,
        Long receiverId,
        Map<String, Object> context
) {

    // ==================== Factory Methods ====================

    /**
     * 새로운 커뮤니티 질문 알림 생성
     */
    public static NotificationRequest newCommunityQuestion(
            Long receiverId,
            Long questionId,
            String questionTitle,
            String questionContent,
            String questionAuthorName,
            String courseName
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("questionId", questionId);
        context.put("questionTitle", questionTitle);
        context.put("questionContent", questionContent);
        context.put("questionAuthorName", questionAuthorName);
        context.put("courseName", courseName);

        return new NotificationRequest(
                NotificationType.NEW_COMMUNITY_QUESTION,
                receiverId,
                context
        );
    }

    /**
     * 새로운 답변 알림 생성
     */
    public static NotificationRequest newAnswer(
            Long receiverId,
            Long questionId,
            Long answerId,
            String questionTitle,
            String answerContent,
            String answerAuthorNickname
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("questionId", questionId);
        context.put("answerId", answerId);
        context.put("questionTitle", questionTitle);
        context.put("answerContent", answerContent);
        context.put("answerAuthorNickname", answerAuthorNickname);

        return new NotificationRequest(
                NotificationType.NEW_COMMUNITY_ANSWER,
                receiverId,
                context
        );
    }

    /**
     * 답변 채택 알림 생성
     */
    public static NotificationRequest answerAccepted(
            Long receiverId,
            Long questionId,
            Long answerId,
            String questionTitle,
            String answerContent
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("questionId", questionId);
        context.put("answerId", answerId);
        context.put("questionTitle", questionTitle);
        context.put("answerContent", answerContent);

        return new NotificationRequest(
                NotificationType.COMMUNITY_ANSWER_ACCEPT,
                receiverId,
                context
        );
    }

    /**
     * 사용자 멘션 알림 생성
     */
    public static NotificationRequest userMentioned(
            Long receiverId,
            Long mentionId,
            String mentionerNickname,
            String content,
            String contentType,
            Long relatedId
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("mentionId", mentionId);
        context.put("mentionerNickname", mentionerNickname);
        context.put("content", content);
        context.put("contentType", contentType);
        context.put("relatedId", relatedId);

        return new NotificationRequest(
                NotificationType.USER_MENTIONED,
                receiverId,
                context
        );
    }

    // ==================== Type-Safe Getters ====================

    public Long getQuestionId() {
        return (Long) context.get("questionId");
    }

    public Long getAnswerId() {
        return (Long) context.get("answerId");
    }

    public Long getMentionId() {
        return (Long) context.get("mentionId");
    }

    public Long getRelatedId() {
        return (Long) context.get("relatedId");
    }

    public String getQuestionTitle() {
        return (String) context.get("questionTitle");
    }

    public String getQuestionContent() {
        return (String) context.get("questionContent");
    }

    public String getQuestionAuthorName() {
        return (String) context.get("questionAuthorName");
    }

    public String getCourseName() {
        return (String) context.get("courseName");
    }

    public String getAnswerContent() {
        return (String) context.get("answerContent");
    }

    public String getAnswerAuthorNickname() {
        return (String) context.get("answerAuthorNickname");
    }

    public String getMentionerNickname() {
        return (String) context.get("mentionerNickname");
    }

    public String getContent() {
        return (String) context.get("content");
    }

    public String getContentType() {
        return (String) context.get("contentType");
    }

    /**
     * 컨텍스트에서 특정 값 가져오기
     */
    public <T> T get(String key, Class<T> type) {
        Object value = context.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
}
