package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
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
class CommunityQuestionFileWriterTest {

    @InjectMocks
    private CommunityQuestionFileWriter fileWriter;
    @Mock
    private FileWriter fileWriterDep;
    @Mock
    private CommunityFileRepository communityFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void saveQuestionFiles_첨부파일없음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        // when & then
        assertThat(fileWriter.saveQuestionFiles(question, null)).isEmpty();
        assertThat(fileWriter.saveQuestionFiles(question, List.of())).isEmpty();
        verifyNoInteractions(fileWriterDep, communityFileRepository);
    }

    @Test
    void saveQuestionFiles_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getId()).thenReturn(1L);
        MockMultipartFile mf = new MockMultipartFile("file", "file.jpg", "image/jpeg", "content".getBytes());
        List<MultipartFile> files = List.of(mf);
        File file = mock(File.class);
        when(fileWriterDep.saveFiles(anyList())).thenReturn(List.of(file));
        CommunityFile communityFile = mock(CommunityFile.class);
        when(communityFileRepository.saveAll(anyList())).thenReturn(List.of(communityFile));
        when(appProperties.getDomain()).thenReturn("domain");

        // when
        List<FileInfo> result = fileWriter.saveQuestionFiles(question, files);
        // then
        assertThat(result).hasSize(1);
        verify(fileWriterDep, times(1)).saveFiles(anyList());
        verify(communityFileRepository, times(1)).saveAll(anyList());
    }

    @Test
    void deleteQuestionFiles_첨부파일없음() {
        // when
        fileWriter.deleteQuestionFiles(null);
        fileWriter.deleteQuestionFiles(List.of());
        // then
        verifyNoInteractions(communityFileRepository);
    }

    @Test
    void deleteQuestionFiles_정상() {
        // given
        CommunityFile file1 = mock(CommunityFile.class);
        CommunityFile file2 = mock(CommunityFile.class);
        List<CommunityFile> files = List.of(file1, file2);
        // when
        fileWriter.deleteQuestionFiles(files);
        // then
        verify(communityFileRepository, times(1)).deleteAll(files);
    }
}