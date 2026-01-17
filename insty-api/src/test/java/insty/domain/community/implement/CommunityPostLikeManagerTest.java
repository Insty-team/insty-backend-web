package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.repository.CommunityPostLikeRepository;
import insty.domain.community.repository.CommunityPostRepository;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityPostLikeManagerTest {

    @Mock
    private CommunityPostLikeRepository communityPostLikeRepository;
    @Mock
    private CommunityPostRepository communityPostRepository;

    @InjectMocks
    private CommunityPostLikeManager communityPostLikeManager;

    @Test
    void likePost_이미좋아요_멱등() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();

        when(communityPostLikeRepository.existsByCommunityPostIdAndUserId(post.getId(), user.getId())).thenReturn(true);
        when(communityPostRepository.findLikeCountById(post.getId())).thenReturn(2);

        CommunityLikeRes res = communityPostLikeManager.likePost(post, user);

        assertThat(res.likeCount()).isEqualTo(2);
        assertThat(res.likedByMe()).isTrue();
        verify(communityPostLikeRepository, never()).save(any());
        verify(communityPostRepository, never()).incrementLikeCount(post.getId());
    }

    @Test
    void likePost_신규좋아요_증가() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();

        when(communityPostLikeRepository.existsByCommunityPostIdAndUserId(post.getId(), user.getId())).thenReturn(false);
        when(communityPostRepository.findLikeCountById(post.getId())).thenReturn(1);

        CommunityLikeRes res = communityPostLikeManager.likePost(post, user);

        assertThat(res.likeCount()).isEqualTo(1);
        assertThat(res.likedByMe()).isTrue();
        verify(communityPostLikeRepository).save(any());
        verify(communityPostRepository).incrementLikeCount(post.getId());
    }

    @Test
    void unlikePost_이미없음_멱등() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();

        when(communityPostLikeRepository.existsByCommunityPostIdAndUserId(post.getId(), user.getId())).thenReturn(false);
        when(communityPostRepository.findLikeCountById(post.getId())).thenReturn(3);

        CommunityLikeRes res = communityPostLikeManager.unlikePost(post, user);

        assertThat(res.likeCount()).isEqualTo(3);
        assertThat(res.likedByMe()).isFalse();
        verify(communityPostLikeRepository, never()).deleteByCommunityPostIdAndUserId(post.getId(), user.getId());
        verify(communityPostRepository, never()).decrementLikeCount(post.getId());
    }

    @Test
    void unlikePost_좋아요제거_감소() {
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId();

        when(communityPostLikeRepository.existsByCommunityPostIdAndUserId(post.getId(), user.getId())).thenReturn(true);
        when(communityPostRepository.findLikeCountById(post.getId())).thenReturn(2);

        CommunityLikeRes res = communityPostLikeManager.unlikePost(post, user);

        assertThat(res.likeCount()).isEqualTo(2);
        assertThat(res.likedByMe()).isFalse();
        verify(communityPostLikeRepository).deleteByCommunityPostIdAndUserId(post.getId(), user.getId());
        verify(communityPostRepository).decrementLikeCount(post.getId());
    }
}
