package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.repository.CommunityCommentLikeRepository;
import insty.domain.community.repository.CommunityCommentRepository;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
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
class CommunityCommentLikeManagerTest {

    @Mock
    private CommunityCommentLikeRepository communityCommentLikeRepository;
    @Mock
    private CommunityCommentRepository communityCommentRepository;

    @InjectMocks
    private CommunityCommentLikeManager communityCommentLikeManager;

    @Test
    void likeComment_이미좋아요_멱등() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        User user = UserFixtureBuilder.getUserWithId();

        when(communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(comment.getId(), user.getId())).thenReturn(true);
        when(communityCommentRepository.findLikeCountById(comment.getId())).thenReturn(2);

        CommunityLikeRes res = communityCommentLikeManager.likeComment(comment, user);

        assertThat(res.likeCount()).isEqualTo(2);
        assertThat(res.likedByMe()).isTrue();
        verify(communityCommentLikeRepository, never()).save(any());
        verify(communityCommentRepository, never()).incrementLikeCount(comment.getId());
    }

    @Test
    void likeComment_신규좋아요_증가() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        User user = UserFixtureBuilder.getUserWithId();

        when(communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(comment.getId(), user.getId())).thenReturn(false);
        when(communityCommentRepository.findLikeCountById(comment.getId())).thenReturn(1);

        CommunityLikeRes res = communityCommentLikeManager.likeComment(comment, user);

        assertThat(res.likeCount()).isEqualTo(1);
        assertThat(res.likedByMe()).isTrue();
        verify(communityCommentLikeRepository).save(any());
        verify(communityCommentRepository).incrementLikeCount(comment.getId());
    }

    @Test
    void unlikeComment_이미없음_멱등() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        User user = UserFixtureBuilder.getUserWithId();

        when(communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(comment.getId(), user.getId())).thenReturn(false);
        when(communityCommentRepository.findLikeCountById(comment.getId())).thenReturn(3);

        CommunityLikeRes res = communityCommentLikeManager.unlikeComment(comment, user);

        assertThat(res.likeCount()).isEqualTo(3);
        assertThat(res.likedByMe()).isFalse();
        verify(communityCommentLikeRepository, never()).deleteByCommunityCommentIdAndUserId(comment.getId(), user.getId());
        verify(communityCommentRepository, never()).decrementLikeCount(comment.getId());
    }

    @Test
    void unlikeComment_좋아요제거_감소() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        User user = UserFixtureBuilder.getUserWithId();

        when(communityCommentLikeRepository.existsByCommunityCommentIdAndUserId(comment.getId(), user.getId())).thenReturn(true);
        when(communityCommentRepository.findLikeCountById(comment.getId())).thenReturn(2);

        CommunityLikeRes res = communityCommentLikeManager.unlikeComment(comment, user);

        assertThat(res.likeCount()).isEqualTo(2);
        assertThat(res.likedByMe()).isFalse();
        verify(communityCommentLikeRepository).deleteByCommunityCommentIdAndUserId(comment.getId(), user.getId());
        verify(communityCommentRepository).decrementLikeCount(comment.getId());
    }
}
