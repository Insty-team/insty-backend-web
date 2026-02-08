package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.VideoInfo;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityPostCreateReq;
import insty.domain.community.dto.CommunityPostDetailsRes;
import insty.domain.community.dto.CommunityPostRes;
import insty.domain.community.dto.CommunityPostSearchReq;
import insty.domain.community.dto.CommunityPostUpdateReq;
import insty.domain.community.dto.CommunityMyPostRes;
import insty.domain.community.dto.CommunityMySearchReq;
import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.implement.CommunityCommentReader;
import insty.domain.community.implement.CommunityPostFileReader;
import insty.domain.community.implement.CommunityPostFileWriter;
import insty.domain.community.implement.CommunityPostReader;
import insty.domain.community.implement.CommunityPostLikeManager;
import insty.domain.community.implement.CommunityPostVideoManager;
import insty.domain.community.implement.CommunityPostWriter;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityPost;
import insty.model.user.User;
import insty.model.video.VideoCommunityPost;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityPostService {

    private final CommunityPostReader communityPostReader;
    private final CommunityPostWriter communityPostWriter;
    private final CommunityPostFileWriter communityPostFileWriter;
    private final CommunityPostFileReader communityPostFileReader;
    private final CommunityPostVideoManager communityPostVideoManager;
    private final CommunityPostLikeManager communityPostLikeManager;
    private final CommunityCommentReader communityCommentReader;
    private final CommunityValidator communityValidator;
    private final UserReader userReader;

    public SearchRes<CommunityPostRes> searchPosts(Long userId, Long courseId, CommunityPostSearchReq req) {
        PageRequest pageRequest = PageRequest.of(req.page() - 1, req.pageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> page = communityPostReader.findPosts(courseId, pageRequest);
        List<CommunityPost> posts = page.getContent();
        List<Long> postIds = posts.stream()
                .map(CommunityPost::getId)
                .toList();
        Map<Long, List<FileInfo>> attachments = communityPostFileReader.getPostFileInfos(postIds);
        Map<Long, VideoInfo> videoInfoByPostId = communityPostVideoManager.getVideosByPostIds(postIds)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> VideoInfo.of(entry.getValue())
                ));
        Map<Long, Long> commentCountByPostId = communityCommentReader.countByPostIds(postIds);
        var likedPostIds = communityPostLikeManager.getLikedPostIds(userId, postIds);
        List<CommunityPostRes> items = posts.stream()
                .map(post -> CommunityPostRes.from(
                        post,
                        attachments.getOrDefault(post.getId(), List.of()),
                        videoInfoByPostId.get(post.getId()),
                        commentCountByPostId.getOrDefault(post.getId(), 0L),
                        likedPostIds.contains(post.getId())
                ))
                .toList();
        PaginationRes paginationRes = PaginationRes.of((int) page.getTotalElements(), req.page(), req.pageSize());
        return SearchRes.from(paginationRes, items);
    }

    public CommunityPostDetailsRes getPostDetails(Long userId, Long courseId, Long postId) {
        CommunityPost post = communityPostReader.getPostWithAttachments(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        List<FileInfo> attachments = communityPostFileReader.getPostFileInfos(post);
        VideoCommunityPost video = communityPostVideoManager.getVideo(post);
        long commentCount = communityCommentReader.countByPostId(postId);
        boolean likedByMe = communityPostLikeManager.isLikedByUser(userId, postId);
        return CommunityPostDetailsRes.from(post, attachments, video, commentCount, likedByMe);
    }

    public CommunityPostDetailsRes createPost(Long userId, Long courseId, CommunityPostCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);
        communityValidator.validatePostFileCountForCreate(attachments);

        var course = communityValidator.validateCourse(courseId);
        User user = userReader.getUser(userId);
        CommunityPost post = communityPostWriter.savePost(user, course, "unused-title", req.content());
        List<FileInfo> fileInfos = communityPostFileWriter.savePostFiles(post, attachments);
        VideoCommunityPost video = communityPostVideoManager.attachVideo(post, req.videoUuid());

        return CommunityPostDetailsRes.from(post, fileInfos, video, 0L, false);
    }

    public CommunityPostDetailsRes updatePost(Long userId, Long courseId, Long postId, CommunityPostUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        CommunityPost post = communityValidator.validatePostExistsWithUserAndCourse(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        communityValidator.validatePostAuthor(userId, post);
        communityValidator.validatePostFileCountForUpdate(postId, attachments, req.deleteFileIds());

        CommunityPost updated = communityPostWriter.updatePost(post, "unused-title", req.content());
        List<FileInfo> fileInfos = communityPostFileWriter.updatePostFiles(updated, attachments, req.deleteFileIds());
        VideoCommunityPost video = communityPostVideoManager.updateAndGetLinkedVideo(updated, req.videoUuid());

        boolean likedByMe = communityPostLikeManager.isLikedByUser(userId, postId);
        long commentCount = communityCommentReader.countByPostId(postId);
        return CommunityPostDetailsRes.from(post, fileInfos, video, commentCount, likedByMe);
    }

    public void deletePost(Long userId, Long courseId, Long postId) {
        CommunityPost post = communityValidator.validatePostExistsWithUserAndCourse(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        communityValidator.validatePostAuthor(userId, post);
        communityPostFileWriter.deletePostFiles(post);
        communityPostVideoManager.deleteVideo(post);
        communityPostWriter.deletePost(post);
    }

    public SearchRes<CommunityMyPostRes> searchMyPosts(Long userId, CommunityMySearchReq req) {
        PageRequest pageRequest = PageRequest.of(req.page() - 1, req.pageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> page = communityPostReader.findPostsByUser(userId, req.keyword(), pageRequest);
        List<CommunityPost> posts = page.getContent();
        List<Long> postIds = posts.stream()
                .map(CommunityPost::getId)
                .toList();
        Map<Long, List<FileInfo>> attachmentsByPostId = communityPostFileReader.getPostFileInfos(postIds);
        Map<Long, VideoInfo> videoInfoByPostId = communityPostVideoManager.getVideosByPostIds(postIds)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> VideoInfo.of(entry.getValue())
                ));

        List<CommunityMyPostRes> items = posts.stream()
                .map(post -> CommunityMyPostRes.from(
                        post,
                        attachmentsByPostId.getOrDefault(post.getId(), List.of()),
                        videoInfoByPostId.get(post.getId())
                ))
                .toList();
        PaginationRes paginationRes = PaginationRes.of((int) page.getTotalElements(), req.page(), req.pageSize());
        return SearchRes.from(paginationRes, items);
    }

    public CommunityLikeRes likePost(Long userId, Long courseId, Long postId) {
        CommunityPost post = communityValidator.validatePostExistsWithUserAndCourse(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        User user = userReader.getUser(userId);
        return communityPostLikeManager.likePost(post, user);
    }

    public CommunityLikeRes unlikePost(Long userId, Long courseId, Long postId) {
        CommunityPost post = communityValidator.validatePostExistsWithUserAndCourse(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        User user = userReader.getUser(userId);
        return communityPostLikeManager.unlikePost(post, user);
    }

}
