package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.VideoEncoding;
import insty.model.video.VideoType;
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
    private VideoCourseRepository videoCourseRepository;
    @Mock
    private VideoAnswerRepository videoAnswerRepository;
    @Mock
    private VideoEncodingRepository videoEncodingRepository;

    @Test
    void getVideoUuid_정상() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long parentId = 1L;

        // mock
        UUID fixedUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(videoCourseRepository.findVideoUuidByCourseId(parentId))
                .thenReturn(Optional.of(fixedUuid));

        // when
        UUID videoUuid = videoReader.getVideoUuid(videoType, parentId);

        // then
        assertThat(videoUuid).isEqualTo(fixedUuid);
    }

    @Test
    void getVideoUuid_에러_존재하지_않는_강의영상() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long parentId = 1L;

        // mock
        when(videoCourseRepository.findVideoUuidByCourseId(parentId))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> videoReader.getVideoUuid(videoType, parentId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void getVideoUuid_에러_존재하지_않는_답변영상() {
        // given
        VideoType videoType = VideoType.ANSWER;
        Long parentId = 1L;

        // mock
        when(videoAnswerRepository.findVideoUuidByCommunityQuestionId(parentId))
                .thenReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> videoReader.getVideoUuid(videoType, parentId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void getVideoUuid_에러_처리되지_않은_영상_타입() {
        // given
        VideoType videoType = mock(VideoType.class);
        Long parentId = 1L;

        // when

        // then
        assertThatThrownBy(() -> videoReader.getVideoUuid(videoType, parentId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

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