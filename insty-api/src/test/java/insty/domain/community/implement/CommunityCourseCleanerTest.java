package insty.domain.community.implement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.community.repository.CommunityCommentRepository;
import insty.domain.community.repository.CommunityPostRepository;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.UserFixtureBuilder;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityCourseCleanerTest {

    @Mock
    private CommunityPostRepository communityPostRepository;
    @Mock
    private CommunityCommentRepository communityCommentRepository;
    @Mock
    private CommunityPostFileWriter communityPostFileWriter;
    @Mock
    private CommunityCommentFileWriter communityCommentFileWriter;
    @Mock
    private CommunityPostVideoManager communityPostVideoManager;
    @Mock
    private CommunityCommentVideoManager communityCommentVideoManager;

    @InjectMocks
    private CommunityCourseCleaner communityCourseCleaner;

    @Test
    void deleteAllByCourseId_댓글과첨부까지삭제() {
        Long courseId = 10L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityComment.create(post, UserFixtureBuilder.getUserWithId(3L), "comment");
        comment.markAsDeleted();
        CommunityComment active = CommunityComment.create(post, UserFixtureBuilder.getUserWithId(4L), "active");

        when(communityPostRepository.findAllByCourse_Id(courseId)).thenReturn(List.of(post));
        when(communityCommentRepository.findAllByCommunityPost_Id(post.getId()))
                .thenReturn(List.of(comment, active));

        communityCourseCleaner.deleteAllByCourseId(courseId);

        verify(communityCommentFileWriter, times(1)).deleteCommentFiles(comment);
        verify(communityCommentVideoManager, times(1)).deleteVideo(comment);
        verify(communityCommentRepository, times(1)).delete(comment);
        verify(communityCommentFileWriter, times(1)).deleteCommentFiles(active);
        verify(communityCommentVideoManager, times(1)).deleteVideo(active);
        verify(communityCommentRepository, times(1)).delete(active);
        verify(communityPostFileWriter, times(1)).deletePostFiles(post);
        verify(communityPostVideoManager, times(1)).deleteVideo(post);
        verify(communityPostRepository, times(1)).delete(post);
    }
}
