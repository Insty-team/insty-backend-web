package insty.domain.notification.common;

import insty.global.property.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationUtils {

    private final AppProperties appProperties;

    public String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    public int getDefaultPreviewLength() {
        return appProperties.getMailPreviewLength();
    }

    public String getDomain() {
        return appProperties.getDomain();
    }
}
