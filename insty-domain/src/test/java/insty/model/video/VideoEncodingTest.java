package insty.model.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class VideoEncodingTest {

    private VideoEncoding videoEncoding;

    @BeforeEach
    void setUp() {
        videoEncoding = VideoEncoding.builder()
                .videoUuid(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .format("hls")
                .encodingS3Key("vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName")
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void getEncodingVideoDirectoryPath_정상() {
        // given

        // when
        String encodingVideoDirectoryPath = videoEncoding.getEncodingVideoDirectoryPath();

        // then
        assertThat(encodingVideoDirectoryPath).isEqualTo("vod/COURSE/hls/00000000-0000-0000-0000-000000000001");
    }

    @Test
    void getHlsMasterFileKey_정상() {
        // given

        // when
        String hlsMasterFileKey = videoEncoding.getHlsMasterFileKey();

        // then
        assertThat(hlsMasterFileKey).isEqualTo("vod/COURSE/hls/00000000-0000-0000-0000-000000000001/fileName.m3u8");
    }

    @Test
    void validateEncodingS3Key_정상() {
        // given

        // when

        // then
        assertThatCode(() -> videoEncoding.validateEncodingS3Key())
                .doesNotThrowAnyException();
    }

    @Test
    void validateEncodingS3Key_에러_키_구조가_유효하지_않음() {
        // given
        videoEncoding = VideoEncoding.builder()
                .videoUuid(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .format("hls")
                .encodingS3Key("vod/fileName") // 잘못된 키
                .createdAt(Instant.now())
                .build();

        // when

        // then
        assertThatThrownBy(() -> videoEncoding.validateEncodingS3Key())
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_INVALID_ENCODING_KEY);
    }
}