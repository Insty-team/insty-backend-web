package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.s3.adapter.S3FileManager;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoFileReaderTest {

    @InjectMocks
    private VideoFileReader videoFileReader;

    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private AppProperties appProperties;

    @Test
    void getThumbnailUrl_정상() {
        // given
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(s3FileManager.doesFileExist(anyString()))
                .thenReturn(true);
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        String thumbnailUrl = videoFileReader.getThumbnailUrl(videoUuid);

        // then
        assertThat(thumbnailUrl).isNotBlank();
    }

    @Test
    void getThumbnailUrl_에러_썸네일이_생성되지_않음() {
        // given
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(s3FileManager.doesFileExist(anyString()))
                .thenReturn(false);

        // when

        // then
        assertThatThrownBy(() -> videoFileReader.getThumbnailUrl(videoUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_BASIC_THUMBNAIL_NOT_FOUND);
    }
}