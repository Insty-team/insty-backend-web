package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
    void updateVideo_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID updateVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        VideoCourse mockVideoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByVideoUuid(updateVideoUuid))
                .thenReturn(Optional.of(mockVideoCourse));
        when(videoCourseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        VideoCourse videoCourse = courseVideoManager.attachmentCourse(course, updateVideoUuid);

        // then
        assertThat(videoCourse.getVideoUuid()).isEqualTo(updateVideoUuid);
    }

    @Test
    void updateVideo_정상_영상을_교체하지_않는다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID updateVideoUuid = null;

        // when
        VideoCourse videoCourse = courseVideoManager.updateVideo(course, updateVideoUuid);

        // then
        assertThat(videoCourse).isNull();
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
}