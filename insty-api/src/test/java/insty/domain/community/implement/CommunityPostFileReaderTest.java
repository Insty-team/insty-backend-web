package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityPostFileRepository;
import insty.global.property.AppProperties;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.community.CommunityPostFile;
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
class CommunityPostFileReaderTest {

    @InjectMocks
    private CommunityPostFileReader communityPostFileReader;

    @Mock
    private AppProperties appProperties;
    @Mock
    private CommunityPostFileRepository communityPostFileRepository;

    @Test
    void getPostFileInfos_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        var file = FileFixtureBuilder.getFileWithId(1L, FileContainerType.COMMUNITY_POST_IMAGE, post.getId(),
                "stored.png", "origin.png", "image/png", 100);
        CommunityPostFile attachment = CommunityPostFile.create(post, file);
        post.getAttachments().add(attachment);
        when(appProperties.getDomain()).thenReturn("insty.test.com");

        // when
        var infos = communityPostFileReader.getPostFileInfos(post);

        // then
        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).url()).isEqualTo("https://insty.test.com/file/COMMUNITY_POST_IMAGE/" + post.getId() + "/stored.png");
    }

    @Test
    void getCurrentFileCount_정상() {
        // given
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        when(communityPostFileRepository.countByCommunityPost_Id(post.getId())).thenReturn(2);

        // when
        int count = communityPostFileReader.getCurrentFileCount(post.getId());

        // then
        assertThat(count).isEqualTo(2);
    }
}
