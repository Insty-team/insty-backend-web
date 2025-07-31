package insty.domain.community.implement;

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
import insty.domain.community.repository.CommunityAnswerFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.file.File;
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
class CommunityAnswerFileWriterTest {

    @InjectMocks
    private CommunityAnswerFileWriter fileWriter;
    @Mock
    private FileWriter fileWriterDep;
    @Mock
    private CommunityAnswerFileRepository communityAnswerFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void saveAnswerFiles_첨부파일없음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result1 = fileWriter.saveAnswerFiles(answer, null);
        List<FileInfo> result2 = fileWriter.saveAnswerFiles(answer, List.of());
        // then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
        verify(communityAnswerFileRepository, atLeastOnce()).findAllByCommunityAnswerId(1L);
    }

    @Test
    void saveAnswerFiles_정상() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> files = List.of(mf);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);
        when(communityAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(communityAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of(communityAnswerFile));
        when(communityAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.saveAnswerFiles(answer, files);
        // then
        assertThat(result).hasSize(1);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(communityAnswerFileRepository, times(1)).saveAll(anyList());
        verify(communityAnswerFileRepository, times(1)).findAllByCommunityAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_정상() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        List<Long> deleteFileIds = List.of(2L, 3L);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);
        when(communityAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(communityAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of(communityAnswerFile));
        when(communityAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, addFiles, deleteFileIds);
        // then
        assertThat(result).hasSize(1);
        verify(communityAnswerFileRepository, times(1)).deleteByAnswerIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(communityAnswerFileRepository, times(1)).saveAll(anyList());
        verify(communityAnswerFileRepository, times(1)).findAllByCommunityAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_추가삭제모두없음() {
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of());
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, null, null);
        assertThat(result).isEmpty();
        verify(communityAnswerFileRepository, never()).deleteByAnswerIdAndFileIdIn(anyLong(), anyList());
        verify(fileWriterDep, never()).saveFiles(anyList());
        verify(communityAnswerFileRepository, never()).saveAll(anyList());
        verify(communityAnswerFileRepository, times(1)).findAllByCommunityAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_추가만있음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        File file = mock(File.class);
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        when(communityAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(communityAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of(communityAnswerFile));
        when(communityAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, addFiles, null);
        // then
        assertThat(result).hasSize(1);
        verify(communityAnswerFileRepository, never()).deleteByAnswerIdAndFileIdIn(anyLong(), anyList());
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(communityAnswerFileRepository, times(1)).saveAll(anyList());
        verify(communityAnswerFileRepository, times(1)).findAllByCommunityAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_삭제만있음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        List<Long> deleteFileIds = List.of(2L, 3L);
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of());
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, null, deleteFileIds);
        // then
        assertThat(result).isEmpty();
        verify(communityAnswerFileRepository, times(1)).deleteByAnswerIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, never()).saveFiles(anyList());
        verify(communityAnswerFileRepository, never()).saveAll(anyList());
        verify(communityAnswerFileRepository, times(1)).findAllByCommunityAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_추가삭제모두있음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> addFiles = List.of(mf);
        List<Long> deleteFileIds = List.of(2L, 3L);
        File file = mock(File.class);
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        when(communityAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(communityAnswerFile));
        when(appProperties.getDomain()).thenReturn("domain");
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of(communityAnswerFile));
        when(communityAnswerFile.getFile()).thenReturn(file);
        // when
        List<FileInfo> result = fileWriter.updateAnswerFiles(answer, addFiles, deleteFileIds);
        // then
        assertThat(result).hasSize(1);
        verify(communityAnswerFileRepository, times(1)).deleteByAnswerIdAndFileIdIn(1L, deleteFileIds);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(communityAnswerFileRepository, times(1)).saveAll(anyList());
        verify(communityAnswerFileRepository, times(1)).findAllByCommunityAnswerId(1L);
    }

    @Test
    void updateAnswerFiles_순서검증() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getId()).thenReturn(1L);
        File fileA = mock(File.class); when(fileA.getId()).thenReturn(1L);
        File fileC = mock(File.class); when(fileC.getId()).thenReturn(3L);
        CommunityAnswerFile cafA = mock(CommunityAnswerFile.class); when(cafA.getFile()).thenReturn(fileA);
        CommunityAnswerFile cafC = mock(CommunityAnswerFile.class); when(cafC.getFile()).thenReturn(fileC);
        MockMultipartFile mfD = new MockMultipartFile("file", "D.jpg", "image/jpeg", "D".getBytes());
        MockMultipartFile mfE = new MockMultipartFile("file", "E.jpg", "image/jpeg", "E".getBytes());
        File fileD = mock(File.class); when(fileD.getId()).thenReturn(4L);
        File fileE = mock(File.class); when(fileE.getId()).thenReturn(5L);
        CommunityAnswerFile cafD = mock(CommunityAnswerFile.class); when(cafD.getFile()).thenReturn(fileD);
        CommunityAnswerFile cafE = mock(CommunityAnswerFile.class); when(cafE.getFile()).thenReturn(fileE);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(fileD, fileE));
        when(communityAnswerFileRepository.saveAll(anyList())).thenReturn(List.of(cafD, cafE));
        when(appProperties.getDomain()).thenReturn("domain");
        when(communityAnswerFileRepository.findAllByCommunityAnswerId(1L)).thenReturn(List.of(cafA, cafC, cafD, cafE));
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
}