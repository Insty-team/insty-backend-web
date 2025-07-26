package insty.domain.video.implement;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.util.FileUtils;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoValidator {

    private static final Map<String, String> EXTENSION_TO_CONTENT_TYPE = Map.of(
            "mp4", "video/mp4",
            "mov", "video/quicktime",
            "webm", "video/webm"
    );

    /**
     * 처리할 수 있는 영상 타입인지 확인하고, 영상 타입이 파일명과 일치하는지 확인한다.<br> mp4 = video/mp4<br> mov = video/quicktime<br> webm =
     * video/webm<br>
     *
     * @param fileName    fileName.mp4
     * @param contentType video/mp4
     */
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
}
