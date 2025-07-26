package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.VideoEncoding;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoReaderTest {

    @InjectMocks
    private VideoReader videoReader;

    @Mock
    private VideoEncodingRepository videoEncodingRepository;

    @Test
    void getVideoEncoding_정상() {
        // given
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(videoEncodingRepository.findByVideoUuid(fixedUuid))
                .thenReturn(Optional.of(mock(VideoEncoding.class)));

        // when
        VideoEncoding videoEncoding = videoReader.getVideoEncoding(fixedUuid);

        // then
        assertThat(videoEncoding).isNotNull();
    }

    @Test
    void getVideoEncoding_에러_존재하지_않는_인코딩영상() {
        // given
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(videoEncodingRepository.findByVideoUuid(fixedUuid))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> videoReader.getVideoEncoding(fixedUuid))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }
}