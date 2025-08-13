package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityQuestionFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityQuestion;
import insty.model.community.CommunityQuestionFile;
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
class CommunityQuestionFileReaderTest {

    @InjectMocks
    private CommunityQuestionFileReader reader;
    @Mock
    private AppProperties appProperties;
    @Mock
    private CommunityQuestionFileRepository communityQuestionFileRepository;

    @Test
    void getQuestionFileInfos_정상_파일존재() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        CommunityQuestionFile file1 = mock(CommunityQuestionFile.class);
        CommunityQuestionFile file2 = mock(CommunityQuestionFile.class);
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
        CommunityQuestion question = mock(CommunityQuestion.class);
        when(question.getAttachments()).thenReturn(List.of());

        // when
        List<FileInfo> result = reader.getQuestionFileInfos(question);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getQuestionFileInfos_정상_attachments가null() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
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
        when(communityQuestionFileRepository.countByCommunityQuestionId(questionId)).thenReturn(3);

        // when
        int result = reader.getCurrentFileCount(questionId);

        // then
        assertThat(result).isEqualTo(3);
        verify(communityQuestionFileRepository).countByCommunityQuestionId(questionId);
    }

    @Test
    void getCurrentFileCount_정상_파일없음() {
        // given
        Long questionId = 1L;
        when(communityQuestionFileRepository.countByCommunityQuestionId(questionId)).thenReturn(0);

        // when
        int result = reader.getCurrentFileCount(questionId);

        // then
        assertThat(result).isEqualTo(0);
        verify(communityQuestionFileRepository).countByCommunityQuestionId(questionId);
    }
}
