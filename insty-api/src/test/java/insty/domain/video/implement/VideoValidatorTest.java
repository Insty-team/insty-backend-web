package insty.domain.video.implement;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoAnswerRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.video.EncodingStatus;
import insty.model.video.VideoAnswer;
import insty.model.video.VideoCourse;
import insty.model.video.VideoFixtureBuilder;
import insty.model.video.VideoType;
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
class VideoValidatorTest {

    @InjectMocks
    private VideoValidator videoValidator;

    @Mock
    private VideoCourseRepository videoCourseRepository;
    @Mock
    private VideoAnswerRepository videoAnswerRepository;

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
    void validateVideoCourseUploadable_정상_오늘_생성한_영상_총_길이가_20분_미만이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoCourseRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 1000, 139));

        // when

        // then
        assertThatCode(() -> videoValidator.validateVideoCourseUploadable(userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateVideoCourseUploadable_에러_오늘_생성한_영상_총_길이가_20분_이상이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoCourseRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 1000, 140));

        // when

        // then
        assertThatThrownBy(() -> videoValidator.validateVideoCourseUploadable(userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
    }

    @Test
    void validateVideoAnswerUploadable_정상_오늘_생성한_영상_총_길이가_5분_미만이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoAnswerRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 239));

        // when

        // then
        assertThatCode(() -> videoValidator.validateVideoAnswerUploadable(userId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateVideoAnswerUploadable_에러_오늘_생성한_영상_총_길이가_5분_이상이다() {
        // given
        Long userId = 1L;

        // mock
        when(videoAnswerRepository.findEncodingDurationByUserIdAndEncodingAtGreaterThan(any(), any()))
                .thenReturn(List.of(60, 240));

        // when

        // then
        assertThatThrownBy(() -> videoValidator.validateVideoAnswerUploadable(userId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_EXCEED_UPLOAD_LIMIT);
    }

    @Test
    void validateReadable_정상() {
        // given
        Long userId = 1L;
        VideoType videoType = VideoType.COURSE;
        Long videoId = 1L;

        // mock
        when(videoCourseRepository.existsByIdAndUserId(videoId, userId))
                .thenReturn(true);

        // when

        // then
        assertThatCode(() -> videoValidator.validateReadable(userId, videoType, videoId))
                .doesNotThrowAnyException();
    }

    @Test
    void validateReadable_에러_영상을_생성한_사람이_아니다() {
        // given
        Long userId = 1L;
        VideoType videoType = VideoType.COURSE;
        Long videoId = 1L;

        // mock
        when(videoCourseRepository.existsByIdAndUserId(videoId, userId))
                .thenReturn(false);

        // when

        // then
        assertThatThrownBy(() -> videoValidator.validateReadable(userId, videoType, videoId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_CANT_READ);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_정상_강의영상() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.COMPLETED);
        when(videoCourseRepository.findByCourseIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatCode(() -> videoValidator.verifyEncodingCompletedAndDeleted(videoType, id))
                .doesNotThrowAnyException();
    }

    @Test
    void verifyEncodingCompletedAndDeleted_정상_답변영상() {
        // given
        VideoType videoType = VideoType.ANSWER;
        Long id = 1L;

        // mock
        VideoAnswer videoAnswer = VideoFixtureBuilder.getVideoAnswerWithIdAndUser();
        ReflectionTestUtils.setField(videoAnswer, "encodingStatus", EncodingStatus.COMPLETED);
        when(videoAnswerRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoAnswer));

        // when

        // then
        assertThatCode(() -> videoValidator.verifyEncodingCompletedAndDeleted(videoType, id))
                .doesNotThrowAnyException();
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_영상이_조회되지_않음() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long id = 1L;

        // when

        // then
        assertThatThrownBy(() -> videoValidator.verifyEncodingCompletedAndDeleted(videoType, id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_처리되지_않은_영상_타입() {
        // given
        VideoType videoType = mock(VideoType.class);
        Long id = 1L;

        // when

        // then
        assertThatThrownBy(() -> videoValidator.verifyEncodingCompletedAndDeleted(videoType, id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_아직_인코딩이_완료되지_않은_영상() {
        // given
        VideoType videoType = VideoType.COURSE;
        Long id = 1L;

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        ReflectionTestUtils.setField(videoCourse, "encodingStatus", EncodingStatus.PROCESSING);
        when(videoCourseRepository.findByCourseIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoCourse));

        // when

        // then
        assertThatThrownBy(() -> videoValidator.verifyEncodingCompletedAndDeleted(videoType, id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }

    @Test
    void verifyEncodingCompletedAndDeleted_에러_답변영상_아직_인코딩이_완료되지_않은_영상() {
        // given
        VideoType videoType = VideoType.ANSWER;
        Long id = 1L;

        // mock
        VideoAnswer videoAnswer = VideoFixtureBuilder.getVideoAnswerWithIdAndUser();
        ReflectionTestUtils.setField(videoAnswer, "encodingStatus", EncodingStatus.PROCESSING);
        when(videoAnswerRepository.findByCommunityQuestionIdAndIsDeleted(id, false))
                .thenReturn(Optional.of(videoAnswer));

        // when

        // then
        assertThatThrownBy(() -> videoValidator.verifyEncodingCompletedAndDeleted(videoType, id))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FINISHED_ENCODING);
    }
}