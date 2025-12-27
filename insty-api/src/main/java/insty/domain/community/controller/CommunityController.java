package insty.domain.community.controller;

import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityCommentCreateReq;
import insty.domain.community.dto.CommunityCommentRes;
import insty.domain.community.dto.CommunityCommentSearchReq;
import insty.domain.community.dto.CommunityCommentUpdateReq;
import insty.domain.community.dto.CommunityPostCreateReq;
import insty.domain.community.dto.CommunityPostDetailsRes;
import insty.domain.community.dto.CommunityPostRes;
import insty.domain.community.dto.CommunityPostSearchReq;
import insty.domain.community.dto.CommunityPostUpdateReq;
import insty.domain.community.service.CommunityCommentService;
import insty.domain.community.service.CommunityPostService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "커뮤니티 API", description = "커뮤니티 게시글/댓글 작성 및 조회 API")
@Validated
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService communityPostService;
    private final CommunityCommentService communityCommentService;

    // ----------------------- 게시글 -----------------------

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/posts")
    public SuccessRes<SearchRes<CommunityPostRes>> searchPosts(
            @ModelAttribute @Valid CommunityPostSearchReq req
    ) {
        return SuccessRes.of(communityPostService.searchPosts(req));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/posts/{postId}")
    public SuccessRes<CommunityPostDetailsRes> getPost(
            @PathVariable @NotNull Long postId
    ) {
        return SuccessRes.of(communityPostService.getPostDetails(postId));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityPostDetailsRes> createPost(
            @CurrentUser Long userId,
            @RequestPart("post") @Valid CommunityPostCreateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityPostService.createPost(userId, req, attachments));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PatchMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityPostDetailsRes> updatePost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long postId,
            @RequestPart("post") @Valid CommunityPostUpdateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityPostService.updatePost(userId, postId, req, attachments));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @DeleteMapping("/posts/{postId}")
    public SuccessRes<?> deletePost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long postId
    ) {
        communityPostService.deletePost(userId, postId);
        return SuccessRes.of(null);
    }

    // ----------------------- 댓글 -----------------------

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/posts/{postId}/comments")
    public SuccessRes<SearchRes<CommunityCommentRes>> getComments(
            @PathVariable @NotNull Long postId,
            @ModelAttribute @Valid CommunityCommentSearchReq req
    ) {
        return SuccessRes.of(communityCommentService.getComments(postId, req));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PostMapping(value = "/posts/{postId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityCommentRes> createComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long postId,
            @RequestPart("comment") @Valid CommunityCommentCreateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityCommentService.createComment(userId, postId, req, attachments));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PatchMapping(value = "/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityCommentRes> updateComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long commentId,
            @RequestPart("comment") @Valid CommunityCommentUpdateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityCommentService.updateComment(userId, commentId, req, attachments));
    }

    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @DeleteMapping("/comments/{commentId}")
    public SuccessRes<?> deleteComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long commentId
    ) {
        communityCommentService.deleteComment(userId, commentId);
        return SuccessRes.of(null);
    }
}
