package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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
    void saveAnswerImageFiles_첨부파일없음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        // when & then
        assertThat(fileWriter.saveAnswerImageFiles(answer, null)).isEmpty();
        assertThat(fileWriter.saveAnswerImageFiles(answer, List.of())).isEmpty();
        verifyNoInteractions(fileWriterDep, communityAnswerFileRepository);
    }

    @Test
    void saveAnswerImageFiles_정상() {
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

        // when
        List<FileInfo> result = fileWriter.saveAnswerImageFiles(answer, files);
        // then
        assertThat(result).hasSize(1);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(communityAnswerFileRepository, times(1)).saveAll(anyList());
    }

    @Test
    void deleteAnswerFiles_첨부파일없음() {
        // when
        fileWriter.deleteAnswerFiles(null);
        fileWriter.deleteAnswerFiles(List.of());
        // then
        verifyNoInteractions(communityAnswerFileRepository);
    }

    @Test
    void deleteAnswerFiles_정상() {
        // given
        CommunityAnswerFile file1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile file2 = mock(CommunityAnswerFile.class);
        List<CommunityAnswerFile> files = List.of(file1, file2);
        // when
        fileWriter.deleteAnswerFiles(files);
        // then
        verify(communityAnswerFileRepository, times(1)).deleteAll(files);
    }
}