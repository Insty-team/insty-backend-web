package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.courseqna.repository.CourseQuestionFileRepository;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CourseQuestionFile;
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
class CourseQuestionFileReaderTest {

    @InjectMocks
    private CourseQuestionFileReader reader;
    @Mock
    private AppProperties appProperties;
    @Mock
    private CourseQuestionFileRepository courseQuestionFileRepository;

    @Test
    void getQuestionFileInfos_정상_파일존재() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        CourseQuestionFile file1 = mock(CourseQuestionFile.class);
        CourseQuestionFile file2 = mock(CourseQuestionFile.class);
        File actualFile1 = mock(File.class);
        File actualFile2 = mock(File.class);
        
        when(question.getAttachments()).thenReturn(List.of(file1, file2));
        when(file1.getFile()).thenReturn(actualFile1);
        when(file2.getFile()).thenReturn(actualFile2);
        when(appProperties.getDomain()).thenReturn("domain");

        // when
        List<FileInfo> result = reader.getQuestionFileInfos(question);

        // then
        assertThat(result).hasSize(2);
        verify(appProperties).getDomain();
    }

    @Test
    void getQuestionFileInfos_정상_파일없음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getAttachments()).thenReturn(List.of());

        // when
        List<FileInfo> result = reader.getQuestionFileInfos(question);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getQuestionFileInfos_정상_attachments가null() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getAttachments()).thenReturn(null);

        // when
        List<FileInfo> result = reader.getQuestionFileInfos(question);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentFileCount_정상() {
        // given
        Long questionId = 1L;
        when(courseQuestionFileRepository.countByCourseQuestionId(questionId)).thenReturn(3);

        // when
        int result = reader.getCurrentFileCount(questionId);

        // then
        assertThat(result).isEqualTo(3);
        verify(courseQuestionFileRepository).countByCourseQuestionId(questionId);
    }

    @Test
    void getCurrentFileCount_정상_파일없음() {
        // given
        Long questionId = 1L;
        when(courseQuestionFileRepository.countByCourseQuestionId(questionId)).thenReturn(0);

        // when
        int result = reader.getCurrentFileCount(questionId);

        // then
        assertThat(result).isEqualTo(0);
        verify(courseQuestionFileRepository).countByCourseQuestionId(questionId);
    }
}
