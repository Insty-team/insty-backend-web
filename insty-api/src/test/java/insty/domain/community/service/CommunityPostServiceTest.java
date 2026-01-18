package insty.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.dto.CommunityMyPostRes;
import insty.domain.community.dto.CommunityMySearchReq;
import insty.domain.community.dto.CommunityPostCreateReq;
import insty.domain.community.dto.CommunityPostDetailsRes;
import insty.domain.community.dto.CommunityPostRes;
import insty.domain.community.dto.CommunityPostSearchReq;
import insty.domain.community.dto.CommunityPostUpdateReq;
import insty.domain.community.implement.CommunityPostFileReader;
import insty.domain.community.implement.CommunityPostFileWriter;
import insty.domain.community.implement.CommunityPostReader;
import insty.domain.community.implement.CommunityPostLikeManager;
import insty.domain.community.implement.CommunityPostVideoManager;
import insty.domain.community.implement.CommunityPostWriter;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityPost;
import insty.model.community.CommunityPostFixtureBuilder;
import insty.model.user.User;
import insty.model.user.UserFixtureBuilder;
import insty.model.video.VideoCommunityPost;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CommunityPostServiceTest {

    @Mock
    private CommunityPostReader communityPostReader;
    @Mock
    private CommunityPostWriter communityPostWriter;
    @Mock
    private CommunityPostFileWriter communityPostFileWriter;
    @Mock
    private CommunityPostFileReader communityPostFileReader;
    @Mock
    private CommunityPostVideoManager communityPostVideoManager;
    @Mock
    private CommunityPostLikeManager communityPostLikeManager;
    @Mock
    private CommunityValidator communityValidator;
    @Mock
    private UserReader userReader;

    @InjectMocks
    private CommunityPostService communityPostService;

    @Test
    void searchPosts_페이지네이션_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        Long courseId = post.getCourse().getId();
        Page<CommunityPost> page = new PageImpl<>(List.of(post), PageRequest.of(0, 10), 1);
        when(communityPostReader.findPosts(eq(courseId), any(PageRequest.class))).thenReturn(page);
        List<FileInfo> attachments = List.of(new FileInfo(1L, "image.png", "image/png", 100, "url"));
        VideoCommunityPost video = VideoCommunityPost.create("video.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());
        when(communityPostFileReader.getPostFileInfos(List.of(post.getId())))
                .thenReturn(Map.of(post.getId(), attachments));
        when(communityPostVideoManager.getVideosByPostIds(List.of(post.getId())))
                .thenReturn(Map.of(post.getId(), video));
        when(communityPostLikeManager.getLikedPostIds(eq(userId), eq(List.of(post.getId())))).thenReturn(Set.of());

        // when
        SearchRes<?> result = communityPostService.searchPosts(userId, courseId, new CommunityPostSearchReq(1, 10));

        // then
        assertThat(result.pagination()).isEqualTo(PaginationRes.of(1, 1, 10));
        assertThat(result.items()).hasSize(1);
        CommunityPostRes resItem = (CommunityPostRes) result.items().get(0);
        assertThat(resItem.attachments()).isEqualTo(attachments);
        assertThat(resItem.videoInfo()).isNotNull();
        assertThat(resItem.likedByMe()).isFalse();
    }

    @Test
    void searchPosts_likedByMe_포함() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        Long courseId = post.getCourse().getId();
        Page<CommunityPost> page = new PageImpl<>(List.of(post), PageRequest.of(0, 10), 1);
        when(communityPostReader.findPosts(eq(courseId), any(PageRequest.class))).thenReturn(page);
        when(communityPostLikeManager.getLikedPostIds(eq(userId), eq(List.of(post.getId())))).thenReturn(Set.of(post.getId()));

        SearchRes<CommunityPostRes> result = communityPostService.searchPosts(userId, courseId, new CommunityPostSearchReq(1, 10));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).likedByMe()).isTrue();
        assertThat(result.items().get(0).likeCount()).isEqualTo(post.getLikeCount());
    }

    @Test
    void getPostDetails_첨부파일과영상_포함() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        List<FileInfo> files = List.of(new FileInfo(1L, "file.png", "image/png", 100, "url"));
        VideoCommunityPost video = VideoCommunityPost.create("video.mp4", UUID.randomUUID(),
                UserFixtureBuilder.getUserWithId());

        when(communityPostReader.getPostWithAttachments(post.getId())).thenReturn(post);
        when(communityPostFileReader.getPostFileInfos(post)).thenReturn(files);
        when(communityPostVideoManager.getVideo(post)).thenReturn(video);
        // when
        CommunityPostDetailsRes res = communityPostService.getPostDetails(userId, post.getCourse().getId(), post.getId());

        // then
        assertThat(res.postId()).isEqualTo(post.getId());
        assertThat(res.attachments()).isEqualTo(files);
        assertThat(res.videoInfo()).isNotNull();
    }

    @Test
    void getPostDetails_likedByMe_포함() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();

        when(communityPostReader.getPostWithAttachments(post.getId())).thenReturn(post);
        when(communityPostFileReader.getPostFileInfos(post)).thenReturn(List.of());
        when(communityPostVideoManager.getVideo(post)).thenReturn(null);
        when(communityPostLikeManager.isLikedByUser(userId, post.getId())).thenReturn(true);

        CommunityPostDetailsRes res = communityPostService.getPostDetails(userId, post.getCourse().getId(), post.getId());

        assertThat(res.likedByMe()).isTrue();
        assertThat(res.likeCount()).isEqualTo(post.getLikeCount());
    }

    @Test
    void searchMyPosts_키워드검색_및_내용포함() {
        // given
        Long userId = 1L;
        String keyword = "검색어";
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        Page<CommunityPost> page = new PageImpl<>(List.of(post), PageRequest.of(0, 5), 1);
        CommunityMySearchReq req = new CommunityMySearchReq(1, 5, keyword);
        when(communityPostReader.findPostsByUser(eq(userId), eq(keyword), any(PageRequest.class))).thenReturn(page);

        // when
        SearchRes<CommunityMyPostRes> res = communityPostService.searchMyPosts(userId, req);

        // then
        assertThat(res.items()).hasSize(1);
        CommunityMyPostRes myPost = res.items().get(0);
        assertThat(myPost.content()).isEqualTo(post.getContent());
        verify(communityPostReader).findPostsByUser(eq(userId), eq(keyword), any(PageRequest.class));
    }

    @Test
    void createPost_정상() {
        // given
        Long userId = 1L;
        User user = UserFixtureBuilder.getUserWithId(userId);
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        var course = post.getCourse();
        List<MultipartFile> attachments = List.of(
                new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{1})
        );
        CommunityPostCreateReq req = new CommunityPostCreateReq("title", "content", UUID.randomUUID());
        List<FileInfo> fileInfos = List.of(new FileInfo(1L, "f1.png", "image/png", 10, "url"));
        VideoCommunityPost video = VideoCommunityPost.create("video.mp4", req.videoUuid(), user);

        when(communityValidator.validateCourse(course.getId())).thenReturn(course);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityPostWriter.savePost(user, course, req.title(), req.content())).thenReturn(post);
        when(communityPostFileWriter.savePostFiles(post, attachments)).thenReturn(fileInfos);
        when(communityPostVideoManager.attachVideo(post, req.videoUuid())).thenReturn(video);

        // when
        CommunityPostDetailsRes res = communityPostService.createPost(userId, course.getId(), req, attachments);

        // then
        assertThat(res.postId()).isEqualTo(post.getId());
        assertThat(res.attachments()).isEqualTo(fileInfos);
        assertThat(res.videoInfo()).isNotNull();
        verify(communityValidator, times(1)).validatePostFileCountForCreate(attachments);
    }

    @Test
    void updatePost_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        Long courseId = post.getCourse().getId();
        CommunityPostUpdateReq req = new CommunityPostUpdateReq("new title", "new content", List.of(1L), null);
        List<MultipartFile> addFiles = List.of(new MockMultipartFile("f1", "f1.png", "image/png", new byte[]{1}));
        List<FileInfo> fileInfos = List.of(new FileInfo(2L, "f1.png", "image/png", 10, "url"));

        when(communityValidator.validatePostExists(post.getId())).thenReturn(post);
        when(communityPostWriter.updatePost(post, req.title(), req.content())).thenAnswer(invocation -> {
            post.update(req.title(), req.content());
            return post;
        });
        when(communityPostFileWriter.updatePostFiles(post, addFiles, req.deleteFileIds())).thenReturn(fileInfos);
        when(communityPostVideoManager.updateAndGetLinkedVideo(post, req.videoUuid())).thenReturn(null);
        when(communityPostLikeManager.isLikedByUser(userId, post.getId())).thenReturn(false);

        // when
        CommunityPostDetailsRes res = communityPostService.updatePost(userId, courseId, post.getId(), req, addFiles);

        // then
        assertThat(res.title()).isEqualTo(req.title());
        assertThat(res.attachments()).isEqualTo(fileInfos);
        verify(communityValidator).validatePostAuthor(userId, post);
    }

    @Test
    void deletePost_정상() {
        // given
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        when(communityValidator.validatePostExists(post.getId())).thenReturn(post);

        // when
        communityPostService.deletePost(userId, post.getCourse().getId(), post.getId());

        // then
        verify(communityPostFileWriter).deletePostFiles(post);
        verify(communityPostVideoManager).deleteVideo(post);
        verify(communityPostWriter).deletePost(post);
    }


    @Test
    void likePost_정상() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(userId);
        CommunityLikeRes expected = new CommunityLikeRes(1, true);

        when(communityValidator.validatePostExists(post.getId())).thenReturn(post);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityPostLikeManager.likePost(post, user)).thenReturn(expected);

        CommunityLikeRes res = communityPostService.likePost(userId, post.getCourse().getId(), post.getId());

        assertThat(res).isEqualTo(expected);
        verify(communityValidator).validatePostBelongsToCourse(post, post.getCourse().getId());
    }

    @Test
    void unlikePost_정상() {
        Long userId = 1L;
        CommunityPost post = CommunityPostFixtureBuilder.getCommunityPostWithIdAndUser();
        User user = UserFixtureBuilder.getUserWithId(userId);
        CommunityLikeRes expected = new CommunityLikeRes(0, false);

        when(communityValidator.validatePostExists(post.getId())).thenReturn(post);
        when(userReader.getUser(userId)).thenReturn(user);
        when(communityPostLikeManager.unlikePost(post, user)).thenReturn(expected);

        CommunityLikeRes res = communityPostService.unlikePost(userId, post.getCourse().getId(), post.getId());

        assertThat(res).isEqualTo(expected);
        verify(communityValidator).validatePostBelongsToCourse(post, post.getCourse().getId());
    }
}
