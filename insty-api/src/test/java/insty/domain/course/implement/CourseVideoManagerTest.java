package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.ai.adapter.AiRequester;
import insty.domain.video.repository.VideoCourseRepository;
import insty.error.VideoErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.video.VideoCourse;
import insty.model.video.VideoFixtureBuilder;
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
        when(videoCourseRepository.findByCourseIdAndIsDeleted(anyLong(), anyBoolean()))
                .thenReturn(Optional.of(VideoFixtureBuilder.getVideoCourseWithIdAndUser()));

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
        when(videoCourseRepository.findByCourseIdAndIsDeleted(anyLong(), anyBoolean()))
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
        when(videoCourseRepository.findByCourseIdAndIsDeleted(anyLong(), anyBoolean()))
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
    void softDeleteCourseVideo_정상() {
        // given
        Long courseId = 1L;

        // mock
        when(videoCourseRepository.findByCourseIdAndIsDeleted(anyLong(), anyBoolean()))
                .thenReturn(Optional.of(VideoFixtureBuilder.getVideoCourseWithIdAndUser()));

        // when

        // then
        assertThatCode(() -> courseVideoManager.softDeleteCourseVideo(courseId))
                .doesNotThrowAnyException();
    }

    @Test
    void softDeleteCourseVideo_에러_영상을_찾을_수_없다() {
        // given
        Long courseId = 1L;

        // mock
        when(videoCourseRepository.findByCourseIdAndIsDeleted(anyLong(), anyBoolean()))
                .thenReturn(Optional.empty());

        // when
        courseVideoManager.softDeleteCourseVideo(courseId);

        // then
        verify(videoCourseRepository, never()).deleteLogicallyById(courseId);
    }
}