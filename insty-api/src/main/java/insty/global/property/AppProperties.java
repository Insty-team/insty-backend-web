package insty.global.property;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppProperties {

    @Value("${app.domain}")
    private String domain;

    @Value("${app.mail.preview-length:100}")
    private int mailPreviewLength;

}
