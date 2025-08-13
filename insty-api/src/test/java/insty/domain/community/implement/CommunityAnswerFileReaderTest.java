package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityAnswerFileRepository;
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

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityAnswerFileReaderTest {

    @InjectMocks
    private CommunityAnswerFileReader reader;
    @Mock
    private AppProperties appProperties;
    @Mock
    private CommunityAnswerFileRepository communityAnswerFileRepository;

    @Test
    void getAnswerFileInfos_정상_파일존재() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        CommunityAnswerFile file1 = mock(CommunityAnswerFile.class);
        CommunityAnswerFile file2 = mock(CommunityAnswerFile.class);
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
        CommunityAnswer answer = mock(CommunityAnswer.class);
        when(answer.getAttachments()).thenReturn(List.of());

        // when
        List<FileInfo> result = reader.getAnswerFileInfos(answer);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getAnswerFileInfos_정상_attachments가null() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
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
        when(communityAnswerFileRepository.countByCommunityAnswerId(answerId)).thenReturn(3);

        // when
        int result = reader.getCurrentFileCount(answerId);

        // then
        assertThat(result).isEqualTo(3);
        verify(communityAnswerFileRepository).countByCommunityAnswerId(answerId);
    }

    @Test
    void getCurrentFileCount_정상_파일없음() {
        // given
        Long answerId = 1L;
        when(communityAnswerFileRepository.countByCommunityAnswerId(answerId)).thenReturn(0);

        // when
        int result = reader.getCurrentFileCount(answerId);

        // then
        assertThat(result).isEqualTo(0);
        verify(communityAnswerFileRepository).countByCommunityAnswerId(answerId);
    }
}
