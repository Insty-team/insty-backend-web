package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.courseqna.repository.CourseAnswerFileRepository;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseAnswerFile;
import insty.model.file.File;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseAnswerFileReaderTest {

    @InjectMocks
    private CourseAnswerFileReader reader;
    @Mock
    private AppProperties appProperties;
    @Mock
    private CourseAnswerFileRepository courseAnswerFileRepository;

    @Test
    void getAnswerFileInfos_정상_파일존재() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        CourseAnswerFile file1 = mock(CourseAnswerFile.class);
        CourseAnswerFile file2 = mock(CourseAnswerFile.class);
        File actualFile1 = mock(File.class);
        File actualFile2 = mock(File.class);
        
        when(answer.getAttachments()).thenReturn(List.of(file1, file2));
        when(file1.getFile()).thenReturn(actualFile1);
        when(file2.getFile()).thenReturn(actualFile2);
        when(appProperties.getDomain()).thenReturn("domain");

        // when
        List<FileInfo> result = reader.getAnswerFileInfos(answer);

        // then
        assertThat(result).hasSize(2);
        verify(appProperties).getDomain();
    }

    @Test
    void getAnswerFileInfos_정상_파일없음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getAttachments()).thenReturn(List.of());

        // when
        List<FileInfo> result = reader.getAnswerFileInfos(answer);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getAnswerFileInfos_정상_attachments가null() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getAttachments()).thenReturn(null);

        // when
        List<FileInfo> result = reader.getAnswerFileInfos(answer);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentFileCount_정상() {
        // given
        Long answerId = 1L;
        when(courseAnswerFileRepository.countByCourseAnswerId(answerId)).thenReturn(3);

        // when
        int result = reader.getCurrentFileCount(answerId);

        // then
        assertThat(result).isEqualTo(3);
        verify(courseAnswerFileRepository).countByCourseAnswerId(answerId);
    }

    @Test
    void getCurrentFileCount_정상_파일없음() {
        // given
        Long answerId = 1L;
        when(courseAnswerFileRepository.countByCourseAnswerId(answerId)).thenReturn(0);

        // when
        int result = reader.getCurrentFileCount(answerId);

        // then
        assertThat(result).isEqualTo(0);
        verify(courseAnswerFileRepository).countByCourseAnswerId(answerId);
    }
}
