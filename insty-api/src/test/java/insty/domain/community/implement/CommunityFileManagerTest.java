package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityAnswer;
import insty.model.community.CommunityAnswerFile;
import insty.model.community.CommunityFile;
import insty.model.community.CommunityQuestion;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.s3.adapter.S3FileManager;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityFileManagerTest {

    @InjectMocks
    private CommunityFileManager communityFileManager;

    @Mock
    private FileWriter fileWriter;

    @Mock
    private CommunityWriter communityWriter;

    @Mock
    private AppProperties appProperties;

    @Mock
    private S3FileManager s3FileManager;

    @Test
    void saveQuestionFiles_정상() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        MultipartFile attachment = mock(MultipartFile.class);
        List<MultipartFile> attachments = List.of(attachment);
        File file = mock(File.class);
        List<File> files = List.of(file);

        // mock
        when(question.getId()).thenReturn(1L);
        when(fileWriter.saveFiles(anyList())).thenReturn(files);
        when(file.getId()).thenReturn(1L);
        when(file.getOriginalName()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getUrl("test.com")).thenReturn("http://test.com/test.jpg");
        when(appProperties.getDomain()).thenReturn("test.com");

        // when
        List<FileInfo> result = communityFileManager.saveQuestionFiles(question, attachments);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(fileWriter, times(1)).saveFiles(anyList());
        verify(communityWriter, times(1)).saveCommunityFiles(anyList());
    }

    @Test
    void saveQuestionFiles_정상_파일이_없음() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        List<MultipartFile> attachments = null;

        // when
        List<FileInfo> result = communityFileManager.saveQuestionFiles(question, attachments);

        // then
        assertThat(result).isEmpty();
        verify(fileWriter, times(0)).saveFiles(anyList());
        verify(communityWriter, times(0)).saveCommunityFiles(anyList());
    }

    @Test
    void saveQuestionFiles_정상_빈_리스트() {
        // given
        CommunityQuestion question = mock(CommunityQuestion.class);
        List<MultipartFile> attachments = List.of();

        // when
        List<FileInfo> result = communityFileManager.saveQuestionFiles(question, attachments);

        // then
        assertThat(result).isEmpty();
        verify(fileWriter, times(0)).saveFiles(anyList());
        verify(communityWriter, times(0)).saveCommunityFiles(anyList());
    }

    @Test
    void saveAnswerImageFiles_정상() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        MultipartFile imageFile = mock(MultipartFile.class);
        List<MultipartFile> imageFiles = List.of(imageFile);
        File file = mock(File.class);
        List<File> files = List.of(file);

        // mock
        when(answer.getId()).thenReturn(1L);
        when(fileWriter.saveFiles(anyList())).thenReturn(files);
        when(file.getId()).thenReturn(1L);
        when(file.getOriginalName()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getUrl("test.com")).thenReturn("http://test.com/test.jpg");
        when(appProperties.getDomain()).thenReturn("test.com");

        // when
        List<FileInfo> result = communityFileManager.saveAnswerImageFiles(answer, imageFiles);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(fileWriter, times(1)).saveFiles(anyList());
        verify(communityWriter, times(1)).saveCommunityAnswerFiles(anyList());
    }

    @Test
    void saveAnswerImageFiles_정상_파일이_없음() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        List<MultipartFile> imageFiles = null;

        // when
        List<FileInfo> result = communityFileManager.saveAnswerImageFiles(answer, imageFiles);

        // then
        assertThat(result).isEmpty();
        verify(fileWriter, times(0)).saveFiles(anyList());
        verify(communityWriter, times(0)).saveCommunityAnswerFiles(anyList());
    }

    @Test
    void saveAnswerImageFiles_정상_빈_리스트() {
        // given
        CommunityAnswer answer = mock(CommunityAnswer.class);
        List<MultipartFile> imageFiles = List.of();

        // when
        List<FileInfo> result = communityFileManager.saveAnswerImageFiles(answer, imageFiles);

        // then
        assertThat(result).isEmpty();
        verify(fileWriter, times(0)).saveFiles(anyList());
        verify(communityWriter, times(0)).saveCommunityAnswerFiles(anyList());
    }

    @Test
    void deleteQuestionFiles_정상() {
        // given
        CommunityFile communityFile = mock(CommunityFile.class);
        File file = mock(File.class);
        List<CommunityFile> existingFiles = List.of(communityFile);

        // mock
        when(communityFile.getFile()).thenReturn(file);
        when(file.getContainerType()).thenReturn(FileContainerType.QUESTION_IMAGE);
        when(file.getContainerId()).thenReturn(1L);
        when(file.getName()).thenReturn("test.jpg");

        // when
        communityFileManager.deleteQuestionFiles(existingFiles);

        // then
        verify(s3FileManager, times(1)).delete(anyString(), anyString(), anyString());
        verify(communityWriter, times(1)).deleteCommunityFiles(existingFiles);
    }

    @Test
    void deleteQuestionFiles_정상_파일이_없음() {
        // given
        List<CommunityFile> existingFiles = null;

        // when
        communityFileManager.deleteQuestionFiles(existingFiles);

        // then
        verify(s3FileManager, times(0)).delete(anyString(), anyString(), anyString());
        verify(communityWriter, times(0)).deleteCommunityFiles(anyList());
    }

    @Test
    void deleteQuestionFiles_정상_빈_리스트() {
        // given
        List<CommunityFile> existingFiles = List.of();

        // when
        communityFileManager.deleteQuestionFiles(existingFiles);

        // then
        verify(s3FileManager, times(0)).delete(anyString(), anyString(), anyString());
        verify(communityWriter, times(0)).deleteCommunityFiles(anyList());
    }

    @Test
    void deleteAnswerFiles_정상() {
        // given
        CommunityAnswerFile communityAnswerFile = mock(CommunityAnswerFile.class);
        File file = mock(File.class);
        List<CommunityAnswerFile> existingFiles = List.of(communityAnswerFile);

        // mock
        when(communityAnswerFile.getFile()).thenReturn(file);
        when(file.getContainerType()).thenReturn(FileContainerType.ANSWER_IMAGE);
        when(file.getContainerId()).thenReturn(1L);
        when(file.getName()).thenReturn("test.jpg");

        // when
        communityFileManager.deleteAnswerFiles(existingFiles);

        // then
        verify(s3FileManager, times(1)).delete(anyString(), anyString(), anyString());
        verify(communityWriter, times(1)).deleteCommunityAnswerFiles(existingFiles);
    }

    @Test
    void deleteAnswerFiles_정상_파일이_없음() {
        // given
        List<CommunityAnswerFile> existingFiles = null;

        // when
        communityFileManager.deleteAnswerFiles(existingFiles);

        // then
        verify(s3FileManager, times(0)).delete(anyString(), anyString(), anyString());
        verify(communityWriter, times(0)).deleteCommunityAnswerFiles(anyList());
    }

    @Test
    void deleteAnswerFiles_정상_빈_리스트() {
        // given
        List<CommunityAnswerFile> existingFiles = List.of();

        // when
        communityFileManager.deleteAnswerFiles(existingFiles);

        // then
        verify(s3FileManager, times(0)).delete(anyString(), anyString(), anyString());
        verify(communityWriter, times(0)).deleteCommunityAnswerFiles(anyList());
    }

    @Test
    void convertToFileInfos_정상() {
        // given
        CommunityFile communityFile = mock(CommunityFile.class);
        File file = mock(File.class);
        List<CommunityFile> communityFiles = List.of(communityFile);

        // mock
        when(communityFile.getFile()).thenReturn(file);
        when(file.getId()).thenReturn(1L);
        when(file.getOriginalName()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getUrl("test.com")).thenReturn("http://test.com/test.jpg");
        when(appProperties.getDomain()).thenReturn("test.com");

        // when
        List<FileInfo> result = communityFileManager.convertToFileInfos(communityFiles);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void convertToFileInfos_정상_파일이_없음() {
        // given
        List<CommunityFile> communityFiles = null;

        // when
        List<FileInfo> result = communityFileManager.convertToFileInfos(communityFiles);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void convertToFileInfos_정상_빈_리스트() {
        // given
        List<CommunityFile> communityFiles = List.of();

        // when
        List<FileInfo> result = communityFileManager.convertToFileInfos(communityFiles);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void convertAnswerFilesToFileInfos_정상() {
        // given
        CommunityAnswerFile answerFile = mock(CommunityAnswerFile.class);
        File file = mock(File.class);
        List<CommunityAnswerFile> answerFiles = List.of(answerFile);

        // mock
        when(answerFile.getFile()).thenReturn(file);
        when(file.getId()).thenReturn(1L);
        when(file.getOriginalName()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(1024L);
        when(file.getUrl("test.com")).thenReturn("http://test.com/test.jpg");
        when(appProperties.getDomain()).thenReturn("test.com");

        // when
        List<FileInfo> result = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    void convertAnswerFilesToFileInfos_정상_파일이_없음() {
        // given
        List<CommunityAnswerFile> answerFiles = null;

        // when
        List<FileInfo> result = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void convertAnswerFilesToFileInfos_정상_빈_리스트() {
        // given
        List<CommunityAnswerFile> answerFiles = List.of();

        // when
        List<FileInfo> result = communityFileManager.convertAnswerFilesToFileInfos(answerFiles);

        // then
        assertThat(result).isEmpty();
    }
}