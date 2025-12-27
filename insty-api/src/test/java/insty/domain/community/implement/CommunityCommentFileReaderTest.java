package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityCommentFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
import insty.model.community.CommunityCommentFile;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.file.FileContainerType;
import insty.model.file.FileFixtureBuilder;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityCommentFileReaderTest {

    @InjectMocks
    private CommunityCommentFileReader communityCommentFileReader;

    @Mock
    private AppProperties appProperties;
    @Mock
    private CommunityCommentFileRepository communityCommentFileRepository;

    @Test
    void getCommentFileInfos_정상() {
        // given
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        var file = FileFixtureBuilder.getFileWithId(1L, FileContainerType.COMMUNITY_COMMENT_IMAGE, comment.getId(),
                "stored.png", "origin.png", "image/png", 100);
        CommunityCommentFile attachment = CommunityCommentFile.create(comment, file);
        comment.getAttachments().add(attachment);
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        var infos = communityCommentFileReader.getCommentFileInfos(comment);

        // then
        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).url()).isEqualTo("https://insty.test.com/file/COMMUNITY_COMMENT_IMAGE/" + comment.getId() + "/stored.png");
    }

    @Test
    void getCurrentFileCount_정상() {
        // given
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        when(communityCommentFileRepository.countByCommunityComment_Id(comment.getId())).thenReturn(1);

        // when
        int count = communityCommentFileReader.getCurrentFileCount(comment.getId());

        // then
        assertThat(count).isEqualTo(1);
    }
}
