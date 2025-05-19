package insty.domain.video.implement;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.util.FileUtils;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VideoValidator {

    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "mp4", "video/mp4",
            "mov", "video/quicktime",
            "webm", "video/webm"
    );

    public void validateContentType(String fileName, String contentType) {
        if (!EXTENSION_TO_CONTENT_TYPE.containsValue(contentType)) {
            throw new CustomException(VideoErrorCode.VIDEO_CONTENT_TYPE_ERROR);
        }
        validateEqualsFormat(fileName, contentType);
    }

    private void validateEqualsFormat(String fileName, String contentType) {
        String extension = FileUtils.extractExtension(fileName)
                .orElseThrow(() -> new CustomException(VideoErrorCode.VIDEO_INVALID_FILE_NAME));
        String expectedContentType = EXTENSION_TO_CONTENT_TYPE.get(extension);
        if (!expectedContentType.equals(contentType)) {
            throw new CustomException(VideoErrorCode.VIDEO_TYPE_NOT_MATCH);
        }
    }

    public void validateUploadable() {
        // TODO - 해당 유저가 업로드할 수 있는지 검증(하루 업로드 제한 등)
    }
}
