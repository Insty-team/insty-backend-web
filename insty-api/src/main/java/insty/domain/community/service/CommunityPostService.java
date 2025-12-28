package insty.domain.community.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.dto.CommunityPostCreateReq;
import insty.domain.community.dto.CommunityPostDetailsRes;
import insty.domain.community.dto.CommunityPostRes;
import insty.domain.community.dto.CommunityPostSearchReq;
import insty.domain.community.dto.CommunityPostUpdateReq;
import insty.domain.community.implement.CommunityPostFileReader;
import insty.domain.community.implement.CommunityPostFileWriter;
import insty.domain.community.implement.CommunityPostReader;
import insty.domain.community.implement.CommunityPostVideoManager;
import insty.domain.community.implement.CommunityPostWriter;
import insty.domain.community.implement.CommunityValidator;
import insty.domain.user.implement.UserReader;
import insty.model.community.CommunityPost;
import insty.model.user.User;
import insty.model.video.VideoCommunityPost;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final CommunityValidator communityValidator;
    private final UserReader userReader;

    public SearchRes<CommunityPostRes> searchPosts(CommunityPostSearchReq req) {
        PageRequest pageRequest = PageRequest.of(req.page() - 1, req.pageSize());
        Page<CommunityPost> page = communityPostReader.findPosts(pageRequest);
        List<CommunityPostRes> items = page.getContent().stream()
                .map(CommunityPostRes::from)
                .toList();
        PaginationRes paginationRes = PaginationRes.of((int) page.getTotalElements(), req.page(), req.pageSize());
        return SearchRes.from(paginationRes, items);
    }

    public CommunityPostDetailsRes getPostDetails(Long postId) {
        CommunityPost post = communityPostReader.getPostWithAttachments(postId);
        List<FileInfo> attachments = communityPostFileReader.getPostFileInfos(post);
        VideoCommunityPost video = communityPostVideoManager.getVideo(post);
        return CommunityPostDetailsRes.from(post, attachments, video);
    }

    public CommunityPostDetailsRes createPost(Long userId, CommunityPostCreateReq req, List<MultipartFile> attachments) {
        communityValidator.validateTitle(req.title());
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);
        communityValidator.validatePostFileCountForCreate(attachments);

        User user = userReader.getUser(userId);
        CommunityPost post = communityPostWriter.savePost(user, req.title(), req.content());
        List<FileInfo> fileInfos = communityPostFileWriter.savePostFiles(post, attachments);
        VideoCommunityPost video = communityPostVideoManager.attachVideo(post, req.videoUuid());

        return CommunityPostDetailsRes.from(post, fileInfos, video);
    }

    public CommunityPostDetailsRes updatePost(Long userId, Long postId, CommunityPostUpdateReq req, List<MultipartFile> attachments) {
        communityValidator.validateTitle(req.title());
        communityValidator.validateContent(req.content());
        communityValidator.validateFiles(attachments);

        CommunityPost post = communityValidator.validatePostExists(postId);
        communityValidator.validatePostAuthor(userId, post);
        communityValidator.validatePostFileCountForUpdate(postId, attachments, req.deleteFileIds());

        CommunityPost updated = communityPostWriter.updatePost(post, req.title(), req.content());
        List<FileInfo> fileInfos = communityPostFileWriter.updatePostFiles(updated, attachments, req.deleteFileIds());
        VideoCommunityPost video = communityPostVideoManager.updateAndGetLinkedVideo(updated, req.videoUuid());

        return CommunityPostDetailsRes.from(updated, fileInfos, video);
    }

    public void deletePost(Long userId, Long postId) {
        CommunityPost post = communityValidator.validatePostExists(postId);
        communityValidator.validatePostAuthor(userId, post);
        communityPostFileWriter.deletePostFiles(post);
        communityPostVideoManager.deleteVideo(post);
        communityPostWriter.deletePost(post);
    }
}
