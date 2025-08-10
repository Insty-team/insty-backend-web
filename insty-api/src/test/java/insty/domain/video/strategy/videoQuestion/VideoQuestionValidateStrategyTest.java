package insty.domain.video.strategy.videoQuestion;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoQuestionRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.global.property.VideoUploadLimitProperties;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoFixtureBuilder;
import insty.model.video.VideoQuestion;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class VideoQuestionValidateStrategyTest {

    @InjectMocks
    private VideoQuestionValidateStrategy videoQuestionValidateStrategy;

    @Mock
    private VideoUploadLimitProperties videoUploadLimitProperties;
    @Mock
    private VideoQuestionRepository videoQuestionRepository;


    @BeforeEach
    void setUp() {
        lenient().when(videoUploadLimitProperties.getQuestion())
                .thenReturn(5);
    }

    @Test
    void validateUploadable_정상_오늘_생성한_영상_총_길이가_5분_미만이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoQuestionRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 239));

        // when

        // then
        assertThatCode(() -> videoQuestionValidateStrategy.validateUploadable(userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUploadable_에러_오늘_생성한_영상_총_길이가_5분_이상이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoQuestionRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 240));

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.validateUploadable(userId))
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
        when(videoQuestionRepository.existsByIdAndUserId(videoId, userId))
                .thenReturn(true);

        // when

        // then
        assertThatCode(() -> videoQuestionValidateStrategy.validateReadable(userId, videoId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateReadable_에러_영상을_생성한_사람이_아니다() {
        // given
        Long userId = 1L;
        Long videoId = 1L;

        // mock
        when(videoQuestionRepository.existsByIdAndUserId(videoId, userId))
                .thenReturn(false);

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.validateReadable(userId, videoId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CANT_READ);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_정상() {
        // given
        Long id = 1L;

        // mock
        VideoQuestion videoQuestion = VideoFixtureBuilder.getVideoQuestionWithIdAndUser();
        ReflectionTestUtils.setField(videoQuestion, "encodingStatus", EncodingStatus.COMPLETED);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoQuestion));

        // when

        // then
        assertThatCode(() -> videoQuestionValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .doesNotThrowAnyException();
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_영상이_조회되지_않음() {
        // given
        Long id = 1L;

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_인코딩_실패된_영상() {
        // given
        Long id = 1L;

        // mock
        VideoQuestion videoQuestion = VideoFixtureBuilder.getVideoQuestionWithIdAndUser();
        ReflectionTestUtils.setField(videoQuestion, "encodingStatus", EncodingStatus.FAILED);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoQuestion));

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_ENCODING_FAILED);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_인코딩_실패_유효하지_않은_영상_길이() {
        // given
        Long id = 1L;

        // mock
        VideoQuestion videoQuestion = VideoFixtureBuilder.getVideoQuestionWithIdAndUser();
        ReflectionTestUtils.setField(videoQuestion, "encodingStatus", EncodingStatus.FAILED_INVALID_VIDEO_LENGTH);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoQuestion));

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_ENCODING_FAILED_INVALID_LENGTH);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_인코딩_실패_음성이_존재하지_않음() {
        // given
        Long id = 1L;

        // mock
        VideoQuestion videoQuestion = VideoFixtureBuilder.getVideoQuestionWithIdAndUser();
        ReflectionTestUtils.setField(videoQuestion, "encodingStatus", EncodingStatus.FAILED_NOT_FOUND_VOICE);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoQuestion));

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_ENCODING_FAILED_NOT_FOUND_VOICE);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_아직_인코딩이_완료되지_않은_영상() {
        // given
        Long id = 1L;

        // mock
        VideoQuestion videoQuestion = VideoFixtureBuilder.getVideoQuestionWithIdAndUser();
        ReflectionTestUtils.setField(videoQuestion, "encodingStatus", EncodingStatus.PROCESSING);
        when(videoQuestionRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoQuestion));

        // when

        // then
        assertThatThrownBy(() -> videoQuestionValidateStrategy.verifyEncodingCompletedAndDeleted(id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }
}