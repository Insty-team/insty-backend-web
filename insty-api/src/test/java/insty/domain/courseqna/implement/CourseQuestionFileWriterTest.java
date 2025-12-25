package insty.domain.courseqna.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.courseqna.repository.CourseQuestionFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseQuestion;
import insty.model.courseqna.CourseQuestionFile;
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
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CourseQuestionFileWriterTest {

    @InjectMocks
    private CommunityQuestionFileWriter fileWriter;
    @Mock
    private FileWriter fileWriterDep;
    @Mock
    private CourseQuestionFileRepository courseQuestionFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void saveQuestionFiles_첨부파일없음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result1 = fileWriter.saveQuestionFiles(question, null);
        List<FileInfo> result2 = fileWriter.saveQuestionFiles(question, List.of());
        // then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        verify(courseQuestionFileRepository, atLeastOnce()).findAllByCourseQuestionId(1L);
    }

    @Test
    void saveQuestionFiles_정상() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> files = List.of(mf);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CourseQuestionFile courseQuestionFile = mock(CourseQuestionFile.class);
        when(courseQuestionFileRepository.saveAll(anyList())).thenReturn(List.of(courseQuestionFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of(courseQuestionFile));
        when(courseQuestionFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.saveQuestionFiles(question, files);
        // then
        assertThat(result).hasSize(1);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseQuestionFileRepository, times(1)).saveAll(anyList());
        verify(courseQuestionFileRepository, times(1)).findAllByCourseQuestionId(1L);
    }

    @Test
    void updateQuestionFiles_정상() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        List<Long> deleteFileIds = List.of(2L, 3L);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CourseQuestionFile courseQuestionFile = mock(CourseQuestionFile.class);
        when(courseQuestionFileRepository.saveAll(anyList())).thenReturn(List.of(courseQuestionFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of(courseQuestionFile));
        when(courseQuestionFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateQuestionFiles(question, addFiles, deleteFileIds);
        // then
        assertThat(result).hasSize(1);
        verify(courseQuestionFileRepository, times(1)).deleteByQuestionIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseQuestionFileRepository, times(1)).saveAll(anyList());
        verify(courseQuestionFileRepository, times(1)).findAllByCourseQuestionId(1L);
    }

    @Test
    void updateQuestionFiles_추가삭제모두없음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result = fileWriter.updateQuestionFiles(question, null, null);
        // then
        assertThat(result).isEmpty();
        verify(courseQuestionFileRepository, never()).deleteByQuestionIdAndFileIdIn(anyLong(), anyList());
        verify(fileWriterDep, never()).saveFiles(anyList());
        verify(courseQuestionFileRepository, never()).saveAll(anyList());
        verify(courseQuestionFileRepository, times(1)).findAllByCourseQuestionId(1L);
    }

    @Test
    void updateQuestionFiles_추가만있음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        File file = mock(File.class);
        CourseQuestionFile courseQuestionFile = mock(CourseQuestionFile.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        when(courseQuestionFileRepository.saveAll(anyList())).thenReturn(List.of(courseQuestionFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of(courseQuestionFile));
        when(courseQuestionFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateQuestionFiles(question, addFiles, null);
        // then
        assertThat(result).hasSize(1);
        verify(courseQuestionFileRepository, never()).deleteByQuestionIdAndFileIdIn(anyLong(), anyList());
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseQuestionFileRepository, times(1)).saveAll(anyList());
        verify(courseQuestionFileRepository, times(1)).findAllByCourseQuestionId(1L);
    }

    @Test
    void updateQuestionFiles_삭제만있음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        List<Long> deleteFileIds = List.of(2L, 3L);
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result = fileWriter.updateQuestionFiles(question, null, deleteFileIds);
        // then
        assertThat(result).isEmpty();
        verify(courseQuestionFileRepository, times(1)).deleteByQuestionIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, never()).saveFiles(anyList());
        verify(courseQuestionFileRepository, never()).saveAll(anyList());
        verify(courseQuestionFileRepository, times(1)).findAllByCourseQuestionId(1L);
    }

    @Test
    void updateQuestionFiles_추가삭제모두있음() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        List<Long> deleteFileIds = List.of(2L, 3L);
        File file = mock(File.class);
        CourseQuestionFile courseQuestionFile = mock(CourseQuestionFile.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        when(courseQuestionFileRepository.saveAll(anyList())).thenReturn(List.of(courseQuestionFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of(courseQuestionFile));
        when(courseQuestionFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateQuestionFiles(question, addFiles, deleteFileIds);
        // then
        assertThat(result).hasSize(1);
        verify(courseQuestionFileRepository, times(1)).deleteByQuestionIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseQuestionFileRepository, times(1)).saveAll(anyList());
        verify(courseQuestionFileRepository, times(1)).findAllByCourseQuestionId(1L);
    }

    @Test
    void updateQuestionFiles_질문이null이면예외() {
        // when & then
        assertThatThrownBy(() -> fileWriter.updateQuestionFiles(null, null, null))
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateQuestionFiles_추가삭제_순서검증() {
        // 기존 파일: A(id=1), B(id=2), C(id=3)
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);
        File fileA = mock(File.class); when(fileA.getId()).thenReturn(1L);
        File fileC = mock(File.class); when(fileC.getId()).thenReturn(3L);
        CourseQuestionFile cfA = mock(CourseQuestionFile.class); when(cfA.getFile()).thenReturn(fileA);
        CourseQuestionFile cfC = mock(CourseQuestionFile.class); when(cfC.getFile()).thenReturn(fileC);
        // 새로 추가될 파일: D(id=4), E(id=5)
        MockMultipartFile mfD = new MockMultipartFile("file", "D.jpg", "image/jpeg", "D".getBytes());
        MockMultipartFile mfE = new MockMultipartFile("file", "E.jpg", "image/jpeg", "E".getBytes());
        File fileD = mock(File.class); when(fileD.getId()).thenReturn(4L);
        File fileE = mock(File.class); when(fileE.getId()).thenReturn(5L);
        CourseQuestionFile cfD = mock(CourseQuestionFile.class); when(cfD.getFile()).thenReturn(fileD);
        CourseQuestionFile cfE = mock(CourseQuestionFile.class); when(cfE.getFile()).thenReturn(fileE);
        // 저장 로직 mock
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(fileD, fileE));
        when(courseQuestionFileRepository.saveAll(anyList())).thenReturn(List.of(cfD, cfE));
        when(appProperties.getDomain()).thenReturn("domain");
        // 최종 파일: A, C, D, E (B는 삭제)
        FileInfo fiA = new FileInfo(1L, "A.jpg", "domain/A.jpg", 100L, "image/jpeg");
        FileInfo fiC = new FileInfo(3L, "C.jpg", "domain/C.jpg", 100L, "image/jpeg");
        FileInfo fiD = new FileInfo(4L, "D.jpg", "domain/D.jpg", 100L, "image/jpeg");
        FileInfo fiE = new FileInfo(5L, "E.jpg", "domain/E.jpg", 100L, "image/jpeg");
        when(courseQuestionFileRepository.findAllByCourseQuestionId(1L)).thenReturn(List.of(cfA, cfC, cfD, cfE));
        when(cfA.getFile()).thenReturn(fileA); when(cfC.getFile()).thenReturn(fileC); when(cfD.getFile()).thenReturn(fileD); when(cfE.getFile()).thenReturn(fileE);
        // when
        List<FileInfo> result = fileWriter.updateQuestionFiles(
                question,
                List.of(mfD, mfE),
                List.of(2L)
        );
        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(3L);
        assertThat(result.get(2).id()).isEqualTo(4L);
        assertThat(result.get(3).id()).isEqualTo(5L);
    }

    @Test
    void deleteQuestionFiles_정상() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);

        // when
        fileWriter.deleteQuestionFiles(question);

        // then
        verify(question).removeAllFiles();
        verify(courseQuestionFileRepository).deleteAllByQuestionId(1L);
        verify(fileWriterDep).deleteAllFile(FileContainerType.QUESTION_IMAGE, 1L);
    }

    @Test
    void deleteQuestionFiles_questionNull이면예외() {
        // when & then
        assertThatThrownBy(() -> fileWriter.deleteQuestionFiles(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteQuestionFiles_순서검증() {
        // given
        CourseQuestion question = mock(CourseQuestion.class);
        when(question.getId()).thenReturn(1L);

        // when
        fileWriter.deleteQuestionFiles(question);

        // then - 순서대로 호출되는지 검증
        verify(question).removeAllFiles();
        verify(courseQuestionFileRepository).deleteAllByQuestionId(1L);
        verify(fileWriterDep).deleteAllFile(FileContainerType.QUESTION_IMAGE, 1L);
    }
}