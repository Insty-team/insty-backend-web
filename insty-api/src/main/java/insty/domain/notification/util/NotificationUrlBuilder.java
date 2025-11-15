package insty.domain.notification.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 알림 URL 생성 유틸리티
 * 알림에서 사용되는 리다이렉트 URL을 생성
 */
@Component
public class NotificationUrlBuilder {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 커뮤니티 질문 상세 페이지 URL 생성
     */
    public String buildQuestionUrl(Long questionId) {
        return String.format("%s/community/questions/%d", frontendUrl, questionId);
    }

    /**
     * 커뮤니티 답변으로 이동하는 URL 생성 (특정 답변으로 스크롤)
     */
    public String buildAnswerUrl(Long questionId, Long answerId) {
        return String.format("%s/community/questions/%d#answer-%d", frontendUrl, questionId, answerId);
    }

    /**
     * 멘션된 컨텐츠로 이동하는 URL 생성
     */
    public String buildMentionUrl(String contentType, Long relatedId) {
        return switch (contentType) {
            case "QUESTION" -> buildQuestionUrl(relatedId);
            case "ANSWER" -> String.format("%s/community/questions/%d", frontendUrl, relatedId);
            case "COMMENT" -> String.format("%s/community/questions/%d", frontendUrl, relatedId);
            default -> frontendUrl + "/community";
        };
    }

    /**
     * 강의 상세 페이지 URL 생성
     */
    public String buildCourseUrl(Long courseId) {
        return String.format("%s/courses/%d", frontendUrl, courseId);
    }
}
