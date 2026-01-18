package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityCommentCreateReq;
import insty.domain.community.dto.CommunityCommentRes;
import insty.domain.community.dto.CommunityCommentSearchReq;
import insty.domain.community.dto.CommunityCommentUpdateReq;
import insty.domain.community.dto.CommunityMyCommentRes;
import insty.domain.community.dto.CommunityMySearchReq;
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
import insty.model.community.CommunityPost;
import insty.model.user.User;
import insty.model.video.VideoCommunityComment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityPostReader communityPostReader;
    private final CommunityCommentReader communityCommentReader;
    private final CommunityCommentWriter communityCommentWriter;
    private final CommunityCommentFileWriter communityCommentFileWriter;
    private final CommunityCommentFileReader communityCommentFileReader;
    private final CommunityCommentVideoManager communityCommentVideoManager;
    private final CommunityCommentLikeManager communityCommentLikeManager;
    private final CommunityValidator communityValidator;
    private final UserReader userReader;

    public SearchRes<CommunityCommentRes> getComments(Long userId, Long courseId, Long postId, CommunityCommentSearchReq req) {
        CommunityPost post = communityPostReader.getPost(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        List<CommunityComment> comments = communityCommentReader.getCommentsByPostId(post.getId());
        PageRequest pageRequest = PageRequest.of(req.page() - 1, req.pageSize());

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), comments.size());
        List<CommunityComment> paged = comments.subList(Math.min(start, comments.size()), end);
        List<Long> commentIds = paged.stream()
                .map(CommunityComment::getId)
                .toList();
        var likedCommentIds = communityCommentLikeManager.getLikedCommentIds(userId, commentIds);

        List<CommunityCommentRes> items = paged.stream()
                .map(comment -> toCommunityCommentRes(
                        comment,
                        communityCommentFileReader.getCommentFileInfos(comment),
                        communityCommentVideoManager.getVideo(comment),
                        likedCommentIds.contains(comment.getId())
                ))
                .toList();

        Page<CommunityCommentRes> page = new PageImpl<>(items, pageRequest, comments.size());
        PaginationRes pagination = PaginationRes.of((int) page.getTotalElements(), req.page(), req.pageSize());
        return SearchRes.from(pagination, page.getContent());
    }

    public CommunityCommentRes createComment(Long userId, Long courseId, Long postId, CommunityCommentCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);
        communityValidator.validateCommentFileCountForCreate(attachments);

        CommunityPost post = communityValidator.validatePostExists(postId);
        communityValidator.validatePostBelongsToCourse(post, courseId);
        User user = userReader.getUser(userId);

        CommunityComment comment = communityCommentWriter.saveComment(post, user, req.content());
        List<FileInfo> files = communityCommentFileWriter.saveCommentFiles(comment, attachments);
        VideoCommunityComment video = communityCommentVideoManager.attachVideo(comment, req.videoUuid());

        return toCommunityCommentRes(comment, files, video,
                communityCommentLikeManager.isLikedByUser(userId, comment.getId()));
    }

    public CommunityCommentRes updateComment(Long userId, Long commentId, CommunityCommentUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        CommunityComment comment = communityValidator.validateCommentExists(commentId);
        communityValidator.validateCommentAuthor(userId, comment);
        communityValidator.validateCommentFileCountForUpdate(commentId, attachments, req.deleteFileIds());

        CommunityComment updated = communityCommentWriter.updateComment(comment, req.content());
        List<FileInfo> files = communityCommentFileWriter.updateCommentFiles(updated, attachments, req.deleteFileIds());
        VideoCommunityComment video = communityCommentVideoManager.updateAndGetLinkedVideo(updated, req.videoUuid());

        return toCommunityCommentRes(updated, files, video,
                communityCommentLikeManager.isLikedByUser(userId, updated.getId()));

    }

    private CommunityCommentRes toCommunityCommentRes(CommunityComment comment,
                                                      List<FileInfo> attachments,
                                                      VideoCommunityComment video,
                                                      boolean likedByMe) {
        return CommunityCommentRes.from(comment, attachments, video, likedByMe);
    }

    public void deleteComment(Long userId, Long commentId) {
        CommunityComment comment = communityValidator.validateCommentExists(commentId);
        communityValidator.validateCommentAuthor(userId, comment);
        communityCommentFileWriter.deleteCommentFiles(comment);
        communityCommentVideoManager.deleteVideo(comment);
        communityCommentWriter.deleteComment(comment);
    }

    public SearchRes<CommunityMyCommentRes> searchMyComments(Long userId, CommunityMySearchReq req) {
        PageRequest pageRequest = PageRequest.of(req.page() - 1, req.pageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityComment> page = communityCommentReader.getCommentsByUser(userId, pageRequest);
        List<CommunityMyCommentRes> items = page.getContent().stream()
                .map(CommunityMyCommentRes::from)
                .toList();
        PaginationRes pagination = PaginationRes.of((int) page.getTotalElements(), req.page(), req.pageSize());
        return SearchRes.from(pagination, items);
    }

    public CommunityLikeRes likeComment(Long userId, Long commentId) {
        CommunityComment comment = communityValidator.validateCommentExists(commentId);
        User user = userReader.getUser(userId);
        return communityCommentLikeManager.likeComment(comment, user);
    }

    public CommunityLikeRes unlikeComment(Long userId, Long commentId) {
        CommunityComment comment = communityValidator.validateCommentExists(commentId);
        User user = userReader.getUser(userId);
        return communityCommentLikeManager.unlikeComment(comment, user);
    }
}
