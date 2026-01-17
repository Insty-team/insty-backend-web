package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityCommentCreateReq;
import insty.domain.community.dto.CommunityCommentRes;
import insty.domain.community.dto.CommunityCommentSearchReq;
import insty.domain.community.dto.CommunityCommentUpdateReq;
import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.implement.CommunityCommentFileReader;
import insty.domain.community.implement.CommunityCommentFileWriter;
import insty.domain.community.implement.CommunityCommentReader;
import insty.domain.community.implement.CommunityCommentLikeManager;
import insty.domain.community.implement.CommunityCommentVideoManager;
import insty.domain.community.implement.CommunityCommentWriter;
import insty.domain.community.implement.CommunityPostReader;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityComment;
import insty.model.community.CommunityCommentFixtureBuilder;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.VideoCommunityComment;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
class CommunityCommentServiceTest {

    @Mock
    private CommunityPostReader communityPostReader;
    @Mock
    private CommunityCommentReader communityCommentReader;
    @Mock
    private CommunityCommentWriter communityCommentWriter;
    @Mock
    private CommunityCommentFileWriter communityCommentFileWriter;
    @Mock
    private CommunityCommentFileReader communityCommentFileReader;
    @Mock
    private CommunityCommentVideoManager communityCommentVideoManager;
    @Mock
    private CommunityCommentLikeManager communityCommentLikeManager;
    @Mock
    private CommunityValidator communityValidator;
    @Mock
    private UserReader userReader;

    @InjectMocks
    private CommunityCommentService communityCommentService;

    @Test
    void getComments_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        when(communityPostReader.getPost(post.getId())).thenReturn(post);
        when(communityCommentReader.getCommentsByPostId(post.getId())).thenReturn(List.of(comment));
        when(communityCommentFileReader.getCommentFileInfos(comment)).thenReturn(List.of());
        when(communityCommentVideoManager.getVideo(comment)).thenReturn(null);
        when(communityCommentLikeManager.getLikedCommentIds(eq(userId), eq(List.of(comment.getId())))).thenReturn(Set.of());

        // when
        SearchRes<CommunityCommentRes> res = communityCommentService.getComments(userId, post.getCourse().getId(), post.getId(),
                new CommunityCommentSearchReq(1, 10));

        // then
        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).commentId()).isEqualTo(comment.getId());
    }

    @Test
    void getComments_likedByMe_포함() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        when(communityPostReader.getPost(post.getId())).thenReturn(post);
        when(communityCommentReader.getCommentsByPostId(post.getId())).thenReturn(List.of(comment));
        when(communityCommentFileReader.getCommentFileInfos(comment)).thenReturn(List.of());
        when(communityCommentVideoManager.getVideo(comment)).thenReturn(null);
        when(communityCommentLikeManager.getLikedCommentIds(eq(userId), eq(List.of(comment.getId()))))
                .thenReturn(Set.of(comment.getId()));

        SearchRes<CommunityCommentRes> res = communityCommentService.getComments(userId, post.getCourse().getId(), post.getId(),
                new CommunityCommentSearchReq(1, 10));

        assertThat(res.items()).hasSize(1);
        assertThat(res.items().get(0).likedByMe()).isTrue();
        assertThat(res.items().get(0).likeCount()).isEqualTo(comment.getLikeCount());
    }

    @Test
    void createComment_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(userId);
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        List<MultipartFile> attachments = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{1})
        );
        CommunityCommentCreateReq req = new CommunityCommentCreateReq("content", UUID.randomUUID());
        List<FileInfo> fileInfos = List.of(new FileInfo(1L, "f1.png", "image/png", 10, "url"));
        VideoCommunityComment video = VideoCommunityComment.create("video.mp4", req.videoUuid(), user);

        when(communityValidator.validatePostExists(post.getId())).thenReturn(post);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityCommentWriter.saveComment(post, user, req.content())).thenReturn(comment);
        when(communityCommentFileWriter.saveCommentFiles(comment, attachments)).thenReturn(fileInfos);
        when(communityCommentVideoManager.attachVideo(comment, req.videoUuid())).thenReturn(video);

        // when
        CommunityCommentRes res = communityCommentService.createComment(userId, post.getCourse().getId(), post.getId(), req, attachments);

        // then
        assertThat(res.commentId()).isEqualTo(comment.getId());
        assertThat(res.attachments()).isEqualTo(fileInfos);
        verify(communityValidator, times(1)).validateCommentFileCountForCreate(attachments);
    }

    @Test
    void updateComment_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        CommunityCommentUpdateReq req = new CommunityCommentUpdateReq("new content", List.of(1L), null);
        List<MultipartFile> addFiles = List.of(new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{1}));
        List<FileInfo> fileInfos = List.of(new FileInfo(2L, "f1.png", "image/png", 10, "url"));

        when(communityValidator.validateCommentExists(comment.getId())).thenReturn(comment);
        when(communityCommentWriter.updateComment(comment, req.content())).thenAnswer(invocation -> {
            comment.update(req.content());
            return comment;
        });
        when(communityCommentFileWriter.updateCommentFiles(comment, addFiles, req.deleteFileIds())).thenReturn(fileInfos);
        when(communityCommentVideoManager.updateAndGetLinkedVideo(comment, req.videoUuid())).thenReturn(null);
        when(communityCommentLikeManager.isLikedByUser(userId, comment.getId())).thenReturn(false);

        // when
        CommunityCommentRes res = communityCommentService.updateComment(userId, comment.getId(), req, addFiles);

        // then
        assertThat(res.content()).isEqualTo(req.content());
        assertThat(res.attachments()).isEqualTo(fileInfos);
        verify(communityValidator).validateCommentAuthor(userId, comment);
    }

    @Test
    void deleteComment_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        when(communityValidator.validateCommentExists(comment.getId())).thenReturn(comment);

        // when
        communityCommentService.deleteComment(userId, comment.getId());

        // then
        verify(communityCommentFileWriter).deleteCommentFiles(comment);
        verify(communityCommentVideoManager).deleteVideo(comment);
        verify(communityCommentWriter).deleteComment(comment);
    }

    @Test
    void likeComment_정상() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        User user = UserFixtureBuilder.getUserWithId(userId);
        CommunityLikeRes expected = new CommunityLikeRes(1, true);

        when(communityValidator.validateCommentExists(comment.getId())).thenReturn(comment);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityCommentLikeManager.likeComment(comment, user)).thenReturn(expected);

        CommunityLikeRes res = communityCommentService.likeComment(userId, comment.getId());

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void unlikeComment_정상() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        CommunityComment comment = CommunityCommentFixtureBuilder.getCommunityCommentWithIdAndUser(post);
        User user = UserFixtureBuilder.getUserWithId(userId);
        CommunityLikeRes expected = new CommunityLikeRes(0, false);

        when(communityValidator.validateCommentExists(comment.getId())).thenReturn(comment);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityCommentLikeManager.unlikeComment(comment, user)).thenReturn(expected);

        CommunityLikeRes res = communityCommentService.unlikeComment(userId, comment.getId());

        assertThat(res).isEqualTo(expected);
    }
}
