package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.file.implement.FileWriter;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.model.course.CourseFixtureBuilder;
import insty.model.course.CoursePracticeFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.file.FileFixtureBuilder;
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
    void saveThumbnail_정상() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // mock
        File file = FileFixtureBuilder.getCourseThumbnailWithId();
        when(fileWriter.saveFile(any()))
                .thenReturn(file);

        // when

        // then
        assertThatCode(() -> courseFileWriter.saveThumbnail(thumbnail, course))
                .doesNotThrowAnyException();
    }

    @Test
    void saveThumbnail_정상_빈_썸네일이면_저장_작업을_하지_않는다() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                new byte[0]);
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when
        courseFileWriter.saveThumbnail(thumbnail, course);

        // then
        verify(fileWriter, never()).saveFile(any());
    }

    @Test
    void savePracticeFilesAndGetInfo_정상() {
        // given
        MockMultipartFile practiceFile = new MockMultipartFile("practiceFile", "practice.jpg", "image/jpeg",
                "content".getBytes());
        List<MultipartFile> practiceFiles = List.of(practiceFile);
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // mock
        File file = FileFixtureBuilder.getCoursePracticeFileWithId();
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
    void savePracticeFilesAndGetInfo_정상_빈_파일이면_저장_작업을_하지_않는다() {
        // given
        List<MultipartFile> practiceFiles = List.of();
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when
        List<FileInfo> fileInfos = courseFileWriter.savePracticeFilesAndGetInfo(practiceFiles, course);

        // then
        assertThat(fileInfos).isNull();
    }

    @Test
    void updateThumbnail_정상() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // mock
        File file = FileFixtureBuilder.getCourseThumbnailWithId();
        when(fileWriter.saveFile(any()))
                .thenReturn(file);

        // when

        // then
        assertThatCode(() -> courseFileWriter.updateThumbnail(thumbnail, course))
                .doesNotThrowAnyException();
    }

    @Test
    void updateThumbnail_정상_빈_썸네일이면_교체_작업을_하지_않는다() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                new byte[0]);
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when
        courseFileWriter.updateThumbnail(thumbnail, course);

        // then
        verify(fileWriter, never()).saveFile(any());
    }

    @Test
    void updateThumbnail_정상_기존_썸네일이_있다면_지우고_작업한다() {
        // given
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg",
                "content".getBytes());
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        File file = FileFixtureBuilder.getCourseThumbnailWithId();
        ReflectionTestUtils.setField(course, "thumbnail", file);

        // mock
        File newFile = FileFixtureBuilder.getFileWithId(2L, FileContainerType.COURSE_THUMBNAIL, 1L,
                "00000000-0000-0000-0000-000000000002.jpg", "new_thumbnail.jpg", "image/jpeg", 10);
        when(fileWriter.saveFile(any()))
                .thenReturn(newFile);

        // when
        courseFileWriter.updateThumbnail(thumbnail, course);

        // then
        verify(fileWriter).deleteFile(any());
        verify(fileWriter).saveFile(any());
    }

    @Test
    void updatePracticeFilesAndGetInfo_정상() {
        // given
        MockMultipartFile practiceFile1 = new MockMultipartFile("practiceFile1", "practice1.jpg", "image/jpeg",
                "content".getBytes());
        MockMultipartFile practiceFile2 = new MockMultipartFile("practiceFile2", "practice2.jpg", "image/jpeg",
                "content".getBytes());
        List<MultipartFile> practiceFiles = List.of(practiceFile1, practiceFile2);
        List<Long> deleteFileIds = List.of(1L);
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        ReflectionTestUtils.setField(course, "practiceFiles", List.of());

        // mock
        File file1 = FileFixtureBuilder.getCoursePracticeFileWithId();
        File file2 = FileFixtureBuilder.getFileWithId(2L, FileContainerType.COURSE_PRACTICE_FILE, 1L,
                "00000000-0000-0000-0000-000000000001.jpg", "practice2.jpg", "image/jpeg", 7);
        when(fileWriter.saveFiles(any()))
                .thenReturn(List.of(file1, file2));
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when
        List<FileInfo> fileInfos = courseFileWriter.updatePracticeFilesAndGetInfo(practiceFiles, deleteFileIds, course);

        // then
        assertThat(fileInfos).isNotNull();
        assertThat(fileInfos.size()).isEqualTo(2);
    }

    @Test
    void updatePracticeFilesAndGetInfo_정상_추가되는_실습파일이_없다() {
        // given
        List<MultipartFile> practiceFiles = null;
        List<Long> deleteFileIds = List.of(1L);
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();
        ReflectionTestUtils.setField(course, "practiceFiles", List.of());

        // mock

        // when
        List<FileInfo> fileInfos = courseFileWriter.updatePracticeFilesAndGetInfo(practiceFiles, deleteFileIds, course);

        // then
        assertThat(fileInfos).isNotNull();
        assertThat(fileInfos.size()).isEqualTo(0);
    }

    @Test
    void updatePracticeFilesAndGetInfo_에러_허용되는_실습_파일_개수를_초과했다() {
        // given
        MockMultipartFile practiceFile1 = new MockMultipartFile("practiceFile1", "practice1.jpg", "image/jpeg",
                "content".getBytes());
        MockMultipartFile practiceFile2 = new MockMultipartFile("practiceFile2", "practice2.jpg", "image/jpeg",
                "content".getBytes());
        List<MultipartFile> practiceFiles = List.of(practiceFile1, practiceFile2);
        List<Long> deleteFileIds = null;
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        File existFile = FileFixtureBuilder.getCoursePracticeFileWithId();
        ReflectionTestUtils.setField(course, "practiceFiles", List.of(CoursePracticeFile.create(course, existFile)));

        // mock
        when(appProperties.getDomain())
                .thenReturn("insty.test.com");

        // when

        // then
        assertThatThrownBy(() -> courseFileWriter.updatePracticeFilesAndGetInfo(practiceFiles, deleteFileIds, course))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CourseErrorCode.COURSE_TOO_MANY_PRACTICE_FILE);
    }

    @Test
    void deleteAllFiles_정상() {
        // given
        Course course = CourseFixtureBuilder.getCourseWithIdAndUser();

        // when

        // then
        assertThatCode(() -> courseFileWriter.deleteAllFiles(course))
                .doesNotThrowAnyException();
    }
}