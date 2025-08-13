package insty.domain.course.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.course.repository.CoursePracticeFileRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.domain.file.repository.FileRepository;
import insty.domain.video.repository.VideoCourseRepository;
import insty.domain.video.repository.VideoEncodingRepository;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.file.FileFixtureBuilder;
import insty.model.video.VideoFixtureBuilder;
import insty.s3.adapter.S3FileManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseCleanerTest {

    @InjectMocks
    private CourseCleaner courseCleaner;

    @Mock
    private S3FileManager s3FileManager;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseTagRepository courseTagRepository;
    @Mock
    private CoursePracticeFileRepository coursePracticeFileRepository;
    @Mock
    private VideoCourseRepository videoCourseRepository;
    @Mock
    private VideoEncodingRepository videoEncodingRepository;
    @Mock
    private FileRepository fileRepository;

    @Test
    void cleanAllData_정상() {
        // given
        Long userId = 1L;

        // mock
        when(courseRepository.findAllIdByUserId(userId))
                .thenReturn(List.of(1L, 2L));
        List<File> courseThumbnails = new ArrayList<>();
        courseThumbnails.add(FileFixtureBuilder.getCourseThumbnailWithId());
        when(fileRepository.findAllByContainerTypeAndContainerIdIn(eq(FileContainerType.COURSE_THUMBNAIL),
                anyList())).thenReturn(courseThumbnails);
        when(fileRepository.findAllByContainerTypeAndContainerIdIn(eq(FileContainerType.COURSE_PRACTICE_FILE),
                anyList())).thenReturn(List.of(FileFixtureBuilder.getCoursePracticeFileWithId()));

        when(videoCourseRepository.findAllByCourseIdIn(any()))
                .thenReturn(List.of(VideoFixtureBuilder.getVideoCourseWithIdAndUser()));
        when(videoEncodingRepository.findAllByVideoUuidIn(any()))
                .thenReturn(List.of(VideoFixtureBuilder.getVideoEncodingWithId()));

        // when
        courseCleaner.cleanAllData(userId);

        // then
        // 썸네일, 실습파일, 영상 총 3개
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(s3FileManager).deleteAllByKeyList(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
    }

    @Test
    void cleanAllData_정상_생성한_강의가_없음() {
        // given
        Long userId = 1L;

        // when

        // then
        assertThatCode(() -> courseCleaner.cleanAllData(userId))
                .doesNotThrowAnyException();
    }
}