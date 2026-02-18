package insty.domain.video.controller;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_KEY_PAIR_ID;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_POLICY;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNATURE;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_MASTER_M3U8_URL;
import static insty.constants.VideoConstants.DOMAIN;
import static insty.constants.VideoConstants.PATH;

import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoHlsPlaylistRes;
import insty.domain.video.dto.VideoThumbnailRes;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.service.VideoService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import insty.model.video.VideoType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "영상 API")
@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "강의 영상 업로드", description = "강의 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/course")
    public SuccessRes<VideoUploadRes> uploadCourse(
            @CurrentUser Long userId,
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForVideoUpload(VideoType.COURSE, userId, req));
    }

    @Operation(summary = "질문 영상 업로드", description = "질문 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/question")
    public SuccessRes<VideoUploadRes> uploadQuestion(
            @CurrentUser Long userId,
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForVideoUpload(VideoType.QUESTION, userId, req));
    }

    @Operation(summary = "답변 영상 업로드", description = "답변 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/answer")
    public SuccessRes<VideoUploadRes> uploadAnswer(
            @CurrentUser Long userId,
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForVideoUpload(VideoType.ANSWER, userId, req));
    }

    @Operation(summary = "커뮤니티 게시글 영상 업로드", description = "커뮤니티 게시글 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/community-post")
    public SuccessRes<VideoUploadRes> uploadCommunityPost(
            @CurrentUser Long userId,
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForVideoUpload(VideoType.COMMUNITY_POST, userId, req));
    }

    @Operation(summary = "커뮤니티 댓글 영상 업로드", description = "커뮤니티 댓글 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/community-comment")
    public SuccessRes<VideoUploadRes> uploadCommunityComment(
            @CurrentUser Long userId,
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForVideoUpload(VideoType.COMMUNITY_COMMENT, userId, req));
    }

    @Operation(summary = "영상 썸네일 조회", description = "영상에 대한 썸네일을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_THUMBNAIL_GET)
    @GetMapping("/{videoUuid}/thumbnail")
    public SuccessRes<VideoThumbnailRes> getThumbnail(
            @PathVariable("videoUuid") UUID videoUuid
    ) {
        return SuccessRes.of(videoService.getThumbnailUrl(videoUuid));
    }

    @Operation(summary = "영상 조회", description = "HLS 영상 url을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_GET)
    @PostMapping("/playlist")
    public ResponseEntity<SuccessRes<VideoHlsPlaylistRes>> getEncodedVideo(
            @CurrentUser Long userId,
            @RequestBody @Validated VideoHlsPlaylistReq req
    ) {
        return getHlsPlaylistWithCookie(videoService.getVideoCookieMap(userId, req));
    }

    @Operation(summary = "영상 미리보기", description = "1분 미리보기 영상 url을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_PREVIEW)
    @PostMapping("/preview")
    public ResponseEntity<SuccessRes<VideoHlsPlaylistRes>> getPreviewVideo(
            @RequestBody @Validated VideoHlsPlaylistReq req
    ) {
        return getHlsPlaylistWithCookie(videoService.getPreviewCookieMap(req));
    }

    private ResponseEntity<SuccessRes<VideoHlsPlaylistRes>> getHlsPlaylistWithCookie(
            Map<String, String> signedCookieMap) {
        String domain = signedCookieMap.get(DOMAIN);
        String path = signedCookieMap.get(PATH);
        String signedUrl = signedCookieMap.get(CLOUDFRONT_SIGNED_MASTER_M3U8_URL);

        ResponseCookie cookie1 = ResponseCookie.from(CLOUDFRONT_KEY_PAIR_ID,
                        signedCookieMap.get(CLOUDFRONT_KEY_PAIR_ID))
                .domain(domain)
                .path(path)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .build();

        ResponseCookie cookie2 = ResponseCookie.from(CLOUDFRONT_SIGNATURE, signedCookieMap.get(CLOUDFRONT_SIGNATURE))
                .domain(domain)
                .path(path)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .build();

        ResponseCookie cookie3 = ResponseCookie.from(CLOUDFRONT_POLICY, signedCookieMap.get(CLOUDFRONT_POLICY))
                .domain(domain)
                .path(path)
                .httpOnly(true)
                .sameSite("None")
                .secure(true)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie1.toString())
                .header(HttpHeaders.SET_COOKIE, cookie2.toString())
                .header(HttpHeaders.SET_COOKIE, cookie3.toString())
                .body(SuccessRes.of(new VideoHlsPlaylistRes(signedUrl)));
    }
}
