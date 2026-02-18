package insty.global.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "video.upload.max-minute")
public class VideoUploadLimitProperties {

    private int course;
    private int question;
    private int answer;
    private int communityPost;
    private int communityComment;
}
