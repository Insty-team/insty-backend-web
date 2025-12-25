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
import insty.domain.courseqna.repository.CourseAnswerFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.courseqna.CourseAnswer;
import insty.model.courseqna.CourseAnswerFile;
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
class CourseAnswerFileWriterTest {

    @InjectMocks
    private CourseAnswerFileWriter fileWriter;
    @Mock
    private FileWriter fileWriterDep;
    @Mock
    private CourseAnswerFileRepository courseAnswerFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void saveAnswerFiles_첨부파일없음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result1 = fileWriter.saveAnswerFiles(answer, null);
        List<FileInfo> result2 = fileWriter.saveAnswerFiles(answer, List.of());
        // then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        verify(courseAnswerFileRepository, atLeastOnce()).findAllByCourseAnswerId(1L);
    }

    @Test
    void saveAnswerFiles_정상() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> files = List.of(mf);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CourseAnswerFile courseAnswerFile = mock(CourseAnswerFile.class);
        when(courseAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(courseAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of(courseAnswerFile));
        when(courseAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.saveAnswerFiles(answer, files);
        // then
        assertThat(result).hasSize(1);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseAnswerFileRepository, times(1)).saveAll(anyList());
        verify(courseAnswerFileRepository, times(1)).findAllByCourseAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_정상() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        List<Long> deleteFileIds = List.of(2L, 3L);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CourseAnswerFile courseAnswerFile = mock(CourseAnswerFile.class);
        when(courseAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(courseAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of(courseAnswerFile));
        when(courseAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, addFiles, deleteFileIds);
        // then
        assertThat(result).hasSize(1);
        verify(courseAnswerFileRepository, times(1)).deleteByAnswerIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseAnswerFileRepository, times(1)).saveAll(anyList());
        verify(courseAnswerFileRepository, times(1)).findAllByCourseAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_추가삭제모두없음() {
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of());
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, null, null);
        assertThat(result).isEmpty();
        verify(courseAnswerFileRepository, never()).deleteByAnswerIdAndFileIdIn(anyLong(), anyList());
        verify(fileWriterDep, never()).saveFiles(anyList());
        verify(courseAnswerFileRepository, never()).saveAll(anyList());
        verify(courseAnswerFileRepository, times(1)).findAllByCourseAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_추가만있음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        File file = mock(File.class);
        CourseAnswerFile courseAnswerFile = mock(CourseAnswerFile.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        when(courseAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(courseAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of(courseAnswerFile));
        when(courseAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, addFiles, null);
        // then
        assertThat(result).hasSize(1);
        verify(courseAnswerFileRepository, never()).deleteByAnswerIdAndFileIdIn(anyLong(), anyList());
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseAnswerFileRepository, times(1)).saveAll(anyList());
        verify(courseAnswerFileRepository, times(1)).findAllByCourseAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_삭제만있음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        List<Long> deleteFileIds = List.of(2L, 3L);
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, null, deleteFileIds);
        // then
        assertThat(result).isEmpty();
        verify(courseAnswerFileRepository, times(1)).deleteByAnswerIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, never()).saveFiles(anyList());
        verify(courseAnswerFileRepository, never()).saveAll(anyList());
        verify(courseAnswerFileRepository, times(1)).findAllByCourseAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_추가삭제모두있음() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        List<Long> deleteFileIds = List.of(2L, 3L);
        File file = mock(File.class);
        CourseAnswerFile courseAnswerFile = mock(CourseAnswerFile.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        when(courseAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(courseAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of(courseAnswerFile));
        when(courseAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, addFiles, deleteFileIds);
        // then
        assertThat(result).hasSize(1);
        verify(courseAnswerFileRepository, times(1)).deleteByAnswerIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(courseAnswerFileRepository, times(1)).saveAll(anyList());
        verify(courseAnswerFileRepository, times(1)).findAllByCourseAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_순서검증() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);
        File fileA = mock(File.class); when(fileA.getId()).thenReturn(1L);
        File fileC = mock(File.class); when(fileC.getId()).thenReturn(3L);
        CourseAnswerFile cafA = mock(CourseAnswerFile.class); when(cafA.getFile()).thenReturn(fileA);
        CourseAnswerFile cafC = mock(CourseAnswerFile.class); when(cafC.getFile()).thenReturn(fileC);
        MockMultipartFile mfD = new MockMultipartFile("file", "D.jpg", "image/jpeg", "D".getBytes());
        MockMultipartFile mfE = new MockMultipartFile("file", "E.jpg", "image/jpeg", "E".getBytes());
        File fileD = mock(File.class); when(fileD.getId()).thenReturn(4L);
        File fileE = mock(File.class); when(fileE.getId()).thenReturn(5L);
        CourseAnswerFile cafD = mock(CourseAnswerFile.class); when(cafD.getFile()).thenReturn(fileD);
        CourseAnswerFile cafE = mock(CourseAnswerFile.class); when(cafE.getFile()).thenReturn(fileE);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(fileD, fileE));
        when(courseAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(cafD, cafE));
        when(appProperties.getDomain()).thenReturn("domain");
        when(courseAnswerFileRepository.findAllByCourseAnswerId(1L)).thenReturn(List.of(cafA, cafC, cafD, cafE));
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(
                answer,
                List.of(mfD, mfE),
                List.of(2L) // B 삭제
        );
        // then
        assertThat(result).hasSize(4);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(1).id()).isEqualTo(3L);
        assertThat(result.get(2).id()).isEqualTo(4L);
        assertThat(result.get(3).id()).isEqualTo(5L);
    }

    @Test
    void updateAnswerImageFiles_answerNull이면예외() {
        // when & then
        assertThatThrownBy(() -> fileWriter.updateAnswerFiles(null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteAnswerFiles_정상() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);

        // when
        fileWriter.deleteAnswerFiles(answer);

        // then
        verify(answer).removeAllFiles();
        verify(courseAnswerFileRepository).deleteAllByAnswerId(1L);
        verify(fileWriterDep).deleteAllFile(FileContainerType.ANSWER_IMAGE, 1L);
    }

    @Test
    void deleteAnswerFiles_answerNull이면예외() {
        // when & then
        assertThatThrownBy(() -> fileWriter.deleteAnswerFiles(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteAnswerFiles_순서검증() {
        // given
        CourseAnswer answer = mock(CourseAnswer.class);
        when(answer.getId()).thenReturn(1L);

        // when
        fileWriter.deleteAnswerFiles(answer);

        // then - 순서대로 호출되는지 검증
        verify(answer).removeAllFiles();
        verify(courseAnswerFileRepository).deleteAllByAnswerId(1L);
        verify(fileWriterDep).deleteAllFile(FileContainerType.ANSWER_IMAGE, 1L);
    }
}