package insty.domain.notification.util;

import insty.global.property.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtils {

    private final AppProperties appProperties;

    /**
     * 콘텐츠를 지정된 길이로 자릅니다.
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

    /**
     * 기본 콘텐츠 자르기 길이를 반환
     */
    public int getDefaultPreviewLength() {
        return appProperties.getMailPreviewLength();
    }

    /**
     * 도메인 URL을 반환
     */
    public String getDomain() {
        return appProperties.getDomain();
    }
}
