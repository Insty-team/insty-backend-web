package insty.domain.notification.common;

import insty.global.property.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 알림 관련 유틸리티 (통합)
 * - 텍스트 처리 (truncate)
 * - URL 생성 (buildXxxUrl)
 * - 설정 조회 (getDefaultPreviewLength, getDomain)
 */
@Component
@RequiredArgsConstructor
public class NotificationUtils {

    private final AppProperties appProperties;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // ==================== 텍스트 처리 ====================

    /**
     * 텍스트를 최대 길이로 자르고 ... 추가
     */
    public String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    // ==================== URL 생성 ====================

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

    // ==================== 설정 조회 ====================

    public int getDefaultPreviewLength() {
        return appProperties.getMailPreviewLength();
    }

    public String getDomain() {
        return appProperties.getDomain();
    }
}
