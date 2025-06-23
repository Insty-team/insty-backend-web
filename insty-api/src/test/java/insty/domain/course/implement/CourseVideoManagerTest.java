package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoCourseRepository;
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
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByVideoUuid(videoUuid))
                .thenReturn(Optional.of(videoCourse));

        // when
        UUID uuid = courseVideoManager.attachmentCourse(course, videoUuid);

        // then
        assertThat(uuid).isEqualTo(videoUuid);
    }

    @Test
    void attachmentCourse_정상_영상을_업로드하지_않았다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID videoUuid = null;

        // mock

        // when
        UUID uuid = courseVideoManager.attachmentCourse(course, videoUuid);

        // then
        assertThat(uuid).isNull();
    }

    @Test
    void updateVideo_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID updateVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        VideoCourse videoCourse = VideoFixtureBuilder.getVideoCourseWithIdAndUser();
        when(videoCourseRepository.findByVideoUuid(updateVideoUuid))
                .thenReturn(Optional.of(videoCourse));

        // when
        UUID uuid = courseVideoManager.attachmentCourse(course, updateVideoUuid);

        // then
        assertThat(uuid).isEqualTo(updateVideoUuid);
    }

    @Test
    void updateVideo_정상_영상을_교체하지_않는다() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        UUID updateVideoUuid = null;

        // when
        UUID uuid = courseVideoManager.updateVideo(course, updateVideoUuid);

        // then
        assertThat(uuid).isNull();
    }

    @Test
    void getAttachVideoUuid_정상() {
        // given
        Long courseId = 1L;

        // mock
        when(videoCourseRepository.findVideoUuidByCourseId(courseId))
                .thenReturn(Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001")));

        // when
        UUID videoUuid = courseVideoManager.getAttachVideoUuid(courseId);

        // then
        assertThat(videoUuid.toString()).isEqualTo("00000000-0000-0000-0000-000000000001");
    }

    @Test
    void getAttachVideoUuid_정상_연결된_강의가_없으면_null을_반환한다() {
        // given
        Long courseId = 1L;

        // mock
        when(videoCourseRepository.findVideoUuidByCourseId(courseId))
                .thenReturn(Optional.empty());

        // when
        UUID videoUuid = courseVideoManager.getAttachVideoUuid(courseId);

        // then
        assertThat(videoUuid).isNull();
    }
}