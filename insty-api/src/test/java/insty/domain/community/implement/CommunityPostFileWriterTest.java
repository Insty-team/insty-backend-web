package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.community.repository.CommunityPostFileRepository;
import insty.domain.file.implement.FileWriter;
import insty.global.property.AppProperties;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.community.CommunityPostFile;
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
class CommunityPostFileWriterTest {

    @InjectMocks
    private CommunityPostFileWriter communityPostFileWriter;

    @Mock
    private FileWriter fileWriter;
    @Mock
    private CommunityPostFileRepository communityPostFileRepository;
    @Mock
    private AppProperties appProperties;

    @Test
    void savePostFiles_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", "content".getBytes())
        );
        File savedFile = FileFixtureBuilder.getFileWithId(1L, FileContainerType.COMMUNITY_POST_IMAGE, post.getId(),
                "stored.png", "f1.png", "image/png", 100);

        when(fileWriter.saveFiles(any())).thenReturn(List.of(savedFile));
        when(communityPostFileRepository.findAllByCommunityPost_Id(post.getId()))
                .thenReturn(List.of(CommunityPostFile.create(post, savedFile)));
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        List<FileInfo> infos = communityPostFileWriter.savePostFiles(post, files);

        // then
        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).name()).isEqualTo("f1.png");
        assertThat(infos.get(0).url()).isEqualTo("https://insty.test.com/file/COMMUNITY_POST_IMAGE/" + post.getId() + "/stored.png");
    }

    @Test
    void savePostFiles_파일없으면_저장안함() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        List<MultipartFile> files = List.of();

        // when
        List<FileInfo> infos = communityPostFileWriter.savePostFiles(post, files);

        // then
        assertThat(infos).isEmpty();
        verify(fileWriter, never()).saveFiles(any());
    }

    @Test
    void updatePostFiles_삭제후추가_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        List<Long> deleteIds = List.of(10L);
        List<MultipartFile> addFiles = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", "content".getBytes())
        );
        File savedFile = FileFixtureBuilder.getFileWithId(2L, FileContainerType.COMMUNITY_POST_IMAGE, post.getId(),
                "stored.png", "f1.png", "image/png", 100);

        when(fileWriter.saveFiles(any())).thenReturn(List.of(savedFile));
        when(communityPostFileRepository.findAllByCommunityPost_Id(post.getId()))
                .thenReturn(List.of(CommunityPostFile.create(post, savedFile)));
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        List<FileInfo> infos = communityPostFileWriter.updatePostFiles(post, addFiles, deleteIds);

        // then
        assertThat(infos).hasSize(1);
        verify(communityPostFileRepository).deleteByPostIdAndFileIds(post.getId(), deleteIds);
    }

    @Test
    void deletePostFiles_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();

        // when & then
        assertThatCode(() -> communityPostFileWriter.deletePostFiles(post)).doesNotThrowAnyException();
        verify(communityPostFileRepository).deleteAllByPostId(post.getId());
        verify(fileWriter).deleteAllFile(FileContainerType.COMMUNITY_POST_IMAGE, post.getId());
    }
}
