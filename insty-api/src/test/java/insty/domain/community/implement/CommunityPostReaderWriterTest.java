package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityPostRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityPostReaderWriterTest {

    @Mock
    private CommunityPostRepository communityPostRepository;

    @InjectMocks
    private CommunityPostReader communityPostReader;
    @InjectMocks
    private CommunityPostWriter communityPostWriter;

    @Test
    void getPost_에러_없으면_반환() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        when(communityPostRepository.findByIdAndIsDeletedFalse(post.getId())).thenReturn(Optional.of(post));

        CommunityPost result = communityPostReader.getPost(post.getId());

        assertThat(result).isEqualTo(post);
    }

    @Test
    void getPost_삭제되었거나없으면_예외() {
        when(communityPostRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityPostReader.getPost(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND);
    }

    @Test
    void findPosts_정상() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        Page<CommunityPost> page = new PageImpl<>(List.of(post), pageRequest, 1);
        when(communityPostRepository.findAllByCourse_IdAndIsDeletedFalse(post.getCourse().getId(), pageRequest)).thenReturn(page);

        Page<CommunityPost> result = communityPostReader.findPosts(post.getCourse().getId(), pageRequest);
        assertThat(result).isEqualTo(page);
    }

    @Test
    void save_update_delete_동작() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        when(communityPostRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityPost saved = communityPostWriter.savePost(UserFixtureBuilder.getUserWithId(),
                post.getCourse(), "unused-title", post.getContent());
        CommunityPost updated = communityPostWriter.updatePost(saved, "unused-title", "new content");
        communityPostWriter.deletePost(updated);

        assertThat(updated.isDeleted()).isTrue();
        verify(communityPostRepository, times(2)).save(any());
    }
}
