package insty.domain.video.strategy.videoCourse;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoCourseRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoCourse;
import insty.model.video.VideoFixtureBuilder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoCourseValidateStrategyTest {

    @InjectMocks
    private VideoCourseValidateStrategy videoCourseValidateStrategy;

    @Mock
    private VideoCourseRepository videoCourseRepository;

    @Test
    void validateUploadable_정상_오늘_생성한_영상_총_길이가_30분_미만이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoCourseRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 1600, 139));

        // when

        // then
        assertThatCode(() -> videoCourseValidateStrategy.validateUploadable(userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUploadable_에러_오늘_생성한_영상_총_길이가_30분_이상이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoCourseRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 1600, 140));

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.validateUploadable(userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
    }

    @Test
    void validateReadable_정상() {
        // given
        Long userId = 1L;
        Long videoId = 1L;

        // mock
        when(videoCourseRepository.existsByIdAndUserId(videoId, userId))
                .thenReturn(true);

        // when

        // then
        assertThatCode(() -> videoCourseValidateStrategy.validateReadable(userId, videoId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateReadable_에러_영상을_생성한_사람이_아니다() {
        // given
        Long userId = 1L;
        Long videoId = 1L;

        // mock
        when(videoCourseRepository.existsByIdAndUserId(videoId, userId))
                .thenReturn(false);

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.validateReadable(userId, videoId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CANT_READ);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_정상() {
        // given
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.COMPLETED);
        when(videoCourseRepository.findByCourseId(id))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatCode(() -> videoCourseValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .doesNotThrowAnyException();
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_영상이_조회되지_않음() {
        // given
        Long id = 1L;

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_인코딩_실패된_영상() {
        // given
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.FAILED);
        when(videoCourseRepository.findByCourseId(id))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_ENCODING_FAILED);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_인코딩_실패_유효하지_않은_영상_길이() {
        // given
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.FAILED_INVALID_VIDEO_LENGTH);
        when(videoCourseRepository.findByCourseId(id))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_ENCODING_FAILED_INVALID_LENGTH);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_인코딩_실패_음성이_존재하지_않음() {
        // given
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.FAILED_NOT_FOUND_VOICE);
        when(videoCourseRepository.findByCourseId(id))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_아직_인코딩이_완료되지_않은_영상() {
        // given
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.PROCESSING);
        when(videoCourseRepository.findByCourseId(id))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatThrownBy(() -> videoCourseValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }
}