package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.VideoType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoValidatorTest {

    private final VideoValidator videoValidator = new VideoValidator();

    @Test
    void validateContentType_정상() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/mp4";

        // when

        // then
        assertThatCode(() -> videoValidator.validateContentType(fileName, contentType))
                .doesNotThrowAnyException();
    }

    @Test
    void validateContentType_에러_지원하는_콘텐츠_타입이_아니다() {
        // given
        String fileName = "fileName.png";
        String contentType = "image/png";

        // when

        // then
        assertThatThrownBy(() -> videoValidator.validateContentType(fileName, contentType))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CONTENT_TYPE_ERROR);
    }

    @Test
    void validateContentType_에러_파일명에_확장자명이_없다() {
        // given
        String fileName = "fileName";
        String contentType = "video/mp4";

        // when

        // then
        assertThatThrownBy(() -> videoValidator.validateContentType(fileName, contentType))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_FILE_NAME);
    }

    @Test
    void validateContentType_에러_파일명과_콘텐츠_타입이_맞지_않다() {
        // given
        String fileName = "fileName.mp4";
        String contentType = "video/quicktime";

        // when

        // then
        assertThatThrownBy(() -> videoValidator.validateContentType(fileName, contentType))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_TYPE_NOT_MATCH);
    }

    @Test
    void validateUploadable_메서드_미완성() {
        // given

        // when

        // then
    }

    @Test
    void validateReadable_메서드_미완성() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long id = 1L;

        // when

        // then
    }
}