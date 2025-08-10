package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.video.VideoCourse;
import insty.model.video.VideoFixtureBuilder;
import insty.s3.adapter.S3FileManager;
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
class CourseVideoManagerTest {

    @InjectMocks
    private CourseVideoManager courseVideoManager;

    @Mock
    private AiRequester aiRequester;
    @Mock
    private S3FileManager s3FileManager;

    @Mock
    private VideoEncodingRepository videoEncodingRepository;
    @Mock
    private VideoCourseRepository videoCourseRepository;

    @Test
    void attachmentCourse_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        VideoCourse mockVideoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByVideoUuid(videoUuid))
                .thenReturn(Optional.of(mockVideoCourse));
        when(videoCourseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        VideoCourse videoCourse = courseVideoManager.attachmentCourse(course, videoUuid);

        // then
        assertThat(videoCourse.getVideoUuid()).isEqualTo(videoUuid);
    }

    @Test
    void updateAndGetLinkedVideo_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID updateVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        VideoCourse mockVideoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByVideoUuid(updateVideoUuid))
                .thenReturn(Optional.of(mockVideoCourse));
        when(videoCourseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(videoCourseRepository.findByCourseId(anyLong()))
                .thenReturn(Optional.of(VideoFixtureBuilder.getVideoCourseWithIdAndUser()));
        when(videoEncodingRepository.findByVideoUuid(any()))
                .thenReturn(Optional.of(VideoFixtureBuilder.getVideoEncodingWithId()));

        // when
        VideoCourse videoCourse = courseVideoManager.updateAndGetLinkedVideo(course, updateVideoUuid);

        // then
        assertThat(videoCourse.getVideoUuid()).isEqualTo(updateVideoUuid);
    }

    @Test
    void updateAndGetLinkedVideo_정상_영상을_교체하지_않으면_연결되어_있는_영상을_반환한다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID updateVideoUuid = null;

        // mock
        VideoCourse mockVideoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByCourseId(anyLong()))
                .thenReturn(Optional.of(mockVideoCourse));

        // when
        VideoCourse videoCourse = courseVideoManager.updateAndGetLinkedVideo(course, updateVideoUuid);

        // then
        assertThat(videoCourse).isNotNull();
        assertThat(videoCourse.getVideoUuid()).isNotNull();
    }

    @Test
    void getAttachCourseVideo_정상() {
        // given
        Long courseId = 1L;

        // mock
        VideoCourse mockVideoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByCourseId(anyLong()))
                .thenReturn(Optional.of(mockVideoCourse));

        // when
        VideoCourse videoCourse = courseVideoManager.getAttachCourseVideo(courseId);

        // then
        assertThat(videoCourse).isNotNull();
    }

    @Test
    void getAttachCourseVideo_에러_존재하지_않는_강의_영상() {
        // given
        Long courseId = 1L;

        // when

        // then
        assertThatThrownBy(() -> courseVideoManager.getAttachCourseVideo(courseId))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(VideoErrorCode.VIDEO_NOT_FOUND);
    }

    @Test
    void deleteCourseVideo_정상() {
        // given
        Long courseId = 1L;

        // mock
        when(videoCourseRepository.findByCourseId(anyLong()))
                .thenReturn(Optional.of(VideoFixtureBuilder.getVideoCourseWithIdAndUser()));
        when(videoEncodingRepository.findByVideoUuid(any()))
                .thenReturn(Optional.of(VideoFixtureBuilder.getVideoEncodingWithId()));

        // when

        // then
        assertThatCode(() -> courseVideoManager.deleteCourseVideo(courseId))
                .doesNotThrowAnyException();
    }

    @Test
    void deleteCourseVideo_정상_영상을_찾을_수_없으면_삭제하지_않는다() {
        // given
        Long courseId = 1L;

        // mock
        when(videoCourseRepository.findByCourseId(anyLong()))
                .thenReturn(Optional.empty());

        // when
        courseVideoManager.deleteCourseVideo(courseId);

        // then
        verify(videoCourseRepository, never()).findByVideoUuid(any());
    }
}