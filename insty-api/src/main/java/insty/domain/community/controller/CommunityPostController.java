package insty.domain.community.controller;

import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityMyPostRes;
import insty.domain.community.dto.CommunityMySearchReq;
import insty.domain.community.dto.CommunityPostCreateReq;
import insty.domain.community.dto.CommunityLikeRes;
import insty.domain.community.dto.CommunityPostDetailsRes;
import insty.domain.community.dto.CommunityPostRes;
import insty.domain.community.dto.CommunityPostSearchReq;
import insty.domain.community.dto.CommunityPostUpdateReq;
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

@Tag(name = "커뮤니티 게시글 API", description = "커뮤니티 게시글 작성 및 조회 API")
@Validated
@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @GetMapping("/courses/{courseId}/posts")
    public SuccessRes<SearchRes<CommunityPostRes>> searchPosts(
            @PathVariable @NotNull Long courseId,
            @ModelAttribute @Valid CommunityPostSearchReq req
    ) {
        return SuccessRes.of(communityPostService.searchPosts(courseId, req));
    }

    @GetMapping("/courses/{courseId}/posts/{postId}")
    public SuccessRes<CommunityPostDetailsRes> getPost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId
    ) {
        return SuccessRes.of(communityPostService.getPostDetails(userId, courseId, postId));
    }

    @PostMapping(value = "/courses/{courseId}/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityPostDetailsRes> createPost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @RequestPart("post") @Valid CommunityPostCreateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityPostService.createPost(userId, courseId, req, attachments));
    }

    @PatchMapping(value = "/courses/{courseId}/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityPostDetailsRes> updatePost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId,
            @RequestPart("post") @Valid CommunityPostUpdateReq req,
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityPostService.updatePost(userId, courseId, postId, req, attachments));
    }

    @DeleteMapping("/courses/{courseId}/posts/{postId}")
    public SuccessRes<?> deletePost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId
    ) {
        communityPostService.deletePost(userId, courseId, postId);
        return SuccessRes.of(null);
    }

    @PostMapping("/courses/{courseId}/posts/{postId}/likes")
    public SuccessRes<CommunityLikeRes> likePost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId
    ) {
        return SuccessRes.of(communityPostService.likePost(userId, courseId, postId));
    }

    @DeleteMapping("/courses/{courseId}/posts/{postId}/likes")
    public SuccessRes<CommunityLikeRes> unlikePost(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long postId
    ) {
        return SuccessRes.of(communityPostService.unlikePost(userId, courseId, postId));
    }

    @GetMapping("/me/posts")
    public SuccessRes<SearchRes<CommunityMyPostRes>> getMyPosts(
            @CurrentUser Long userId,
            @ModelAttribute @Valid CommunityMySearchReq req
    ) {
        return SuccessRes.of(communityPostService.searchMyPosts(userId, req));
    }
}
