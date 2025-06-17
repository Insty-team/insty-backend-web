package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.course.CoursePracticeFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseFileReaderTest {

    @InjectMocks
    private CourseFileReader courseFileReader;

    @Mock
    private AppProperties appProperties;

    @Test
    void getThumbnailUrl_정상() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        ReflectionTestUtils.setField(course, "thumbnail", file);
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        String thumbnailUrl = courseFileReader.getThumbnailUrl(course, videoUuid);

        // then
        assertThat(thumbnailUrl).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
    }

    @Test
    void getThumbnailUrl_정상_썸네일이_없으면_기본_썸네일을_반환한다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        UUID videoUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        String thumbnailUrl = courseFileReader.getThumbnailUrl(course, videoUuid);

        // then
        assertThat(thumbnailUrl).isEqualTo(
                "https://insty.test.com/file/VIDEO_BASIC_THUMBNAIL/00000000-0000-0000-0000-000000000001/basic_thumbnail.0000000.jpg");
    }

    @Test
    void getThumbnailUrl_정상_썸네일이_없고_video_uuid도_null이면_null을_반환한다() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        UUID videoUuid = null;

        // when
        String thumbnailUrl = courseFileReader.getThumbnailUrl(course, videoUuid);

        // then
        assertThat(thumbnailUrl).isNull();
    }

    @Test
    void getPracticeFiles_정상() {
        // given
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);
        File existFile = File.create(FileContainerType.COURSE_PRACTICE_FILE, 1L,
                "00000000-0000-0000-0000-000000000001.jpg", "practice1.jpg", "image/jpeg", 7);
        ReflectionTestUtils.setField(existFile, "id", 1L);
        ReflectionTestUtils.setField(course, "practiceFiles", List.of(CoursePracticeFile.create(course, existFile)));

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        List<FileInfo> practiceFiles = courseFileReader.getPracticeFiles(course);

        // then
        assertThat(practiceFiles).isNotNull();
        assertThat(practiceFiles.size()).isEqualTo(1);
        assertThat(practiceFiles.get(0).name()).isEqualTo("practice1.jpg");
        assertThat(practiceFiles.get(0).url()).isEqualTo(
                "https://insty.test.com/file/COURSE_PRACTICE_FILE/1/00000000-0000-0000-0000-000000000001.jpg");
    }
}