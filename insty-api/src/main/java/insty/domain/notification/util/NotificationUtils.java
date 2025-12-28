package insty.domain.notification.util;

import insty.global.property.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtils {

    private final AppProperties appProperties;

    /* 텍스트를 최대 길이로 자르고 ... 추가 */
    public String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /* 강좌 질문 상세 페이지 URL 생성 */
    public String buildQuestionUrl(Long questionId) {
        return String.format("%s/course/questions/%d", appProperties.getDomain(), questionId);
    }

    /* 강좌 답변으로 이동하는 URL 생성 (특정 답변으로 스크롤) */
    public String buildAnswerUrl(Long questionId, Long answerId) {
        return String.format("%s/course/questions/%d#answer-%d", appProperties.getDomain(), questionId, answerId);
    }

    /* 멘션된 컨텐츠로 이동하는 URL 생성 */
    public String buildMentionUrl(String contentType, Long relatedId) {
        return switch (contentType) {
            case "QUESTION" -> buildQuestionUrl(relatedId);
            case "ANSWER" -> String.format("%s/course/questions/%d", appProperties.getDomain(), relatedId);
            case "COMMENT" -> String.format("%s/course/questions/%d", appProperties.getDomain(), relatedId);
            default -> appProperties.getDomain() + "/course";
        };
    }

    /* 강의 상세 페이지 URL 생성 */
    public String buildCourseUrl(Long courseId) {
        return String.format("%s/courses/%d", appProperties.getDomain(), courseId);
    }
}
