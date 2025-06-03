package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseFileWriterTest {

    @InjectMocks
    private CourseFileWriter courseFileWriter;

    @Mock
    private FileWriter fileWriter;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CoursePracticeFileRepository coursePracticeFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void saveThumbnailAndGetUrl_정상() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);

        // mock
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        when(fileWriter.saveFile(any()))
                .thenReturn(file);
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        String uploadName = courseFileWriter.saveThumbnailAndGetUrl(thumbnail, course);

        // then
        assertThat(uploadName).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
    }

    @Test
    void saveThumbnailAndGetUrl_정상_빈_썸네일이면_저장_작업을_하지_않는다() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                new byte[0]);
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);

        // when
        String uploadName = courseFileWriter.saveThumbnailAndGetUrl(thumbnail, course);

        // then
        assertThat(uploadName).isNull();
    }

    @Test
    void savePracticeFilesAndGetInfo_정상() {
        // given
        MockMultipartFile practiceFile = new MockMultipartFile("practiceFile", "practice.jpg", "image/jpeg",
                "content".getBytes());
        List<MultipartFile> practiceFiles = List.of(practiceFile);
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);

        // mock
        File file = File.create(FileContainerType.COURSE_PRACTICE_FILE, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "practice.jpg", "image/jpeg", 7);
        when(fileWriter.saveFiles(any()))
                .thenReturn(List.of(file));
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        List<FileInfo> fileInfos = courseFileWriter.savePracticeFilesAndGetInfo(practiceFiles, course);

        // then
        assertThat(fileInfos).isNotNull();
        assertThat(fileInfos.size()).isEqualTo(1);
        assertThat(fileInfos.get(0).name()).isEqualTo("practice.jpg");
        assertThat(fileInfos.get(0).contentType()).isEqualTo("image/jpeg");
        assertThat(fileInfos.get(0).size()).isGreaterThan(0);
        assertThat(fileInfos.get(0).url()).isEqualTo(
                "https://insty.test.com/file/COURSE_PRACTICE_FILE/1/00000000-0000-0000-0000-000000000001.jpg");
    }

    @Test
    void updateThumbnailAndGetUrl_정상() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);
        ReflectionTestUtils.setField(course, "id", 1L);

        // mock
        File file = File.create(FileContainerType.COURSE_THUMBNAIL, 1L, "00000000-0000-0000-0000-000000000001.jpg",
                "thumb.jpg", "image/jpeg", 10);
        when(fileWriter.saveFile(any()))
                .thenReturn(file);
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        String uploadName = courseFileWriter.updateThumbnailAndGetUrl(thumbnail, course);

        // then
        assertThat(uploadName).isEqualTo(
                "https://insty.test.com/file/COURSE_THUMBNAIL/1/00000000-0000-0000-0000-000000000001.jpg");
    }

    @Test
    void updateThumbnailAndGetUrl_정상_빈_썸네일이면_교체_작업을_하지_않는다() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                new byte[0]);
        Course course = Course.create("제목", "설명", 10000, "강의 추천 대상자", true);

        // when
        String uploadName = courseFileWriter.updateThumbnailAndGetUrl(thumbnail, course);

        // then
        assertThat(uploadName).isNull();
    }
}