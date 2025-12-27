package insty.domain.community.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.error.CommunityErrorCode;
import insty.exception.CustomException;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
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

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityCommentReaderWriterTest {

    @Mock
    private CommunityCommentRepository communityCommentRepository;

    @InjectMocks
    private CommunityCommentReader communityCommentReader;
    @InjectMocks
    private CommunityCommentWriter communityCommentWriter;

    @Test
    void getCommentsByPostId_정상() {
        Long postId = 1L;
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        when(communityCommentRepository.findAllByCommunityPost_IdAndIsDeletedFalse(postId))
                .thenReturn(List.of(comment));

        List<CommunityComment> res = communityCommentReader.getCommentsByPostId(postId);

        assertThat(res).hasSize(1);
        assertThat(res.get(0)).isEqualTo(comment);
    }

    @Test
    void getComment_없는경우_예외() {
        when(communityCommentRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> communityCommentReader.getComment(1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
    }

    @Test
    void save_update_delete_동작() {
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(
                CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser());
        when(communityCommentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CommunityComment saved = communityCommentWriter.saveComment(comment.getCommunityPost(),
                UserFixtureBuilder.getUserWithId(), "content");
        CommunityComment updated = communityCommentWriter.updateComment(saved, "new content");
        communityCommentWriter.deleteComment(updated);

        assertThat(updated.getContent()).isEqualTo("new content");
        assertThat(updated.isDeleted()).isTrue();
        verify(communityCommentRepository).save(updated);
    }
}
