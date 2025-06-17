package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import insty.domain.video.repository.VideoCourseRepository;
import insty.model.course.Course;
import insty.model.video.VideoCourse;
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
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(videoCourseRepository.findByVideoUuid(videoUuid))
                .thenReturn(Optional.of(mock(VideoCourse.class)));

        // when

        // then
        assertThatCode(() -> courseVideoManager.attachmentCourse(course, videoUuid))
                .doesNotThrowAnyException();
    }

    @Test
    void attachmentCourse_정상_영상을_업로드하지_않았다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        UUID videoUuid = null;

        // mock

        // when

        // then
        assertThatCode(() -> courseVideoManager.attachmentCourse(course, videoUuid))
                .doesNotThrowAnyException();
    }

    @Test
    void updateVideo_정상() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        UUID updateVideoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(videoCourseRepository.findByVideoUuid(updateVideoUuid))
                .thenReturn(Optional.of(mock(VideoCourse.class)));

        // when

        // then
        assertThatCode(() -> courseVideoManager.updateVideo(course, updateVideoUuid))
                .doesNotThrowAnyException();
    }

    @Test
    void updateVideo_정상_영상을_교체하지_않는다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        UUID updateVideoUuid = null;

        // when

        // then
        assertThatCode(() -> courseVideoManager.updateVideo(course, updateVideoUuid))
                .doesNotThrowAnyException();
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