package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityCommentFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.community.CommunityCommentFile;
import insty.model.file.File;
import insty.model.file.FileContainerType;
import insty.model.file.FileFixtureBuilder;
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
class CommunityCommentFileWriterTest {

    @InjectMocks
    private CommunityCommentFileWriter communityCommentFileWriter;

    @Mock
    private FileWriter fileWriter;
    @Mock
    private CommunityCommentFileRepository communityCommentFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void saveCommentFiles_정상() {
        // given
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", "content".getBytes())
        );
        File savedFile = FileFixtureBuilder.getFileWithId(1L, FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId(),
                "stored.png", "f1.png", "image/png", 100);

        when(fileWriter.saveFiles(any())).thenReturn(List.of(savedFile));
        when(communityCommentFileRepository.findAllByCommunityComment_Id(comment.getId()))
                .thenReturn(List.of(CommunityCommentFile.create(comment, savedFile)));
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        List<FileInfo> infos = communityCommentFileWriter.saveCommentFiles(comment, files);

        // then
        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).name()).isEqualTo("f1.png");
        assertThat(infos.get(0).url()).isEqualTo("https://insty.test.com/file/COMMUNITY_COMMENT_IMAGE/" + comment.getId() + "/stored.png");
    }

    @Test
    void saveCommentFiles_파일없으면_저장안함() {
        // given
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        List<MultipartFile> files = List.of();

        // when
        List<FileInfo> infos = communityCommentFileWriter.saveCommentFiles(comment, files);

        // then
        assertThat(infos).isEmpty();
        verify(fileWriter, never()).saveFiles(any());
    }

    @Test
    void updateCommentFiles_삭제후추가_정상() {
        // given
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        List<Long> deleteIds = List.of(10L);
        List<MultipartFile> addFiles = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", "content".getBytes())
        );
        File savedFile = FileFixtureBuilder.getFileWithId(2L, FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId(),
                "stored.png", "f1.png", "image/png", 100);

        when(fileWriter.saveFiles(any())).thenReturn(List.of(savedFile));
        when(communityCommentFileRepository.findAllByCommunityComment_Id(comment.getId()))
                .thenReturn(List.of(CommunityCommentFile.create(comment, savedFile)));
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        List<FileInfo> infos = communityCommentFileWriter.updateCommentFiles(comment, addFiles, deleteIds);

        // then
        assertThat(infos).hasSize(1);
        verify(communityCommentFileRepository).deleteByCommentIdAndFileIds(comment.getId(), deleteIds);
    }

    @Test
    void deleteCommentFiles_정상() {
        // given
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());

        // when & then
        assertThatCode(() -> communityCommentFileWriter.deleteCommentFiles(comment)).doesNotThrowAnyException();
        verify(communityCommentFileRepository).deleteAllByCommentId(comment.getId());
        verify(fileWriter).deleteAllFile(FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId());
    }
}
