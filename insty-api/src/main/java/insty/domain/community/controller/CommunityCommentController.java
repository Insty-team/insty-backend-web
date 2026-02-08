package insty.domain.community.controller;

import static insty.domain.common.AttachmentConstraints.MAX_COMMENT_FILE_COUNT;
import static insty.domain.common.AttachmentConstraints.COMMENT_ATTACHMENT_DESCRIPTION;

import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityCommentCreateReq;
import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.dto.CommunityCommentRes;
import insty.domain.community.dto.CommunityCommentSearchReq;
import insty.domain.community.dto.CommunityCommentUpdateReq;
import insty.domain.community.dto.CommunityMyCommentRes;
import insty.domain.community.dto.CommunityMySearchReq;
import insty.domain.community.service.CommunityCommentService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

@Tag(name = "커뮤니티 댓글 API", description = "커뮤니티 댓글 작성 및 조회 API")
@Validated
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;

    @GetMapping("/courses/{courseId}/posts/{postId}/comments")
    public SuccessRes<SearchRes<CommunityCommentRes>> getComments(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId,
            @ModelAttribute @Valid CommunityCommentSearchReq req
    ) {
        return SuccessRes.of(communityCommentService.getComments(userId, courseId, postId, req));
    }

    @PostMapping(value = "/courses/{courseId}/posts/{postId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityCommentRes> createComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId,
            @RequestPart("comment") @Valid CommunityCommentCreateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_COMMENT_FILE_COUNT) @Schema(description = COMMENT_ATTACHMENT_DESCRIPTION) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityCommentService.createComment(userId, courseId, postId, req, attachments));
    }

    @PatchMapping(value = "/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityCommentRes> updateComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long commentId,
            @RequestPart("comment") @Valid CommunityCommentUpdateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_COMMENT_FILE_COUNT) @Schema(description = COMMENT_ATTACHMENT_DESCRIPTION) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityCommentService.updateComment(userId, commentId, req, attachments));
    }

    @DeleteMapping("/comments/{commentId}")
    public SuccessRes<?> deleteComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long commentId
    ) {
        communityCommentService.deleteComment(userId, commentId);
        return SuccessRes.of(null);
    }

    @PostMapping("/comments/{commentId}/likes")
    public SuccessRes<CommunityLikeRes> likeComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long commentId
    ) {
        return SuccessRes.of(communityCommentService.likeComment(userId, commentId));
    }

    @DeleteMapping("/comments/{commentId}/likes")
    public SuccessRes<CommunityLikeRes> unlikeComment(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long commentId
    ) {
        return SuccessRes.of(communityCommentService.unlikeComment(userId, commentId));
    }

    @GetMapping("/me/comments")
    public SuccessRes<SearchRes<CommunityMyCommentRes>> getMyComments(
            @CurrentUser Long userId,
            @ModelAttribute @Valid CommunityMySearchReq req
    ) {
        return SuccessRes.of(communityCommentService.searchMyComments(userId, req));
    }

}
