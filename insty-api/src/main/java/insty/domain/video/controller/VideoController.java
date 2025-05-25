package insty.domain.video.controller;

import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_EXPIRES;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_KEY_PAIR_ID;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNATURE;
import static insty.cloudfront.constant.CloudFrontConstants.CLOUDFRONT_SIGNED_URL;
import static insty.constants.VideoConstants.HLS_MASTER_FILE;

import insty.domain.video.dto.VideoHlsPlaylistReq;
import insty.domain.video.dto.VideoHlsPlaylistRes;
import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.service.VideoService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "영상 API")
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "강의 영상 업로드", description = "강의 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/course")
    public SuccessRes<VideoUploadRes> uploadCourse(
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForCourseVideoUpload(req));
    }

    @Operation(summary = "답변 영상 업로드", description = "답변 영상을 업로드하기 위한 URL을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_UPLOAD)
    @PostMapping("/upload/answer")
    public SuccessRes<VideoUploadRes> uploadAnswer(
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForAnswerVideoUpload(req));
    }

    @Operation(summary = "영상 조회", description = "HLS 영상 url을 제공받는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.VIDEO_GET)
    @PostMapping("/playlist")
    public ResponseEntity<SuccessRes<VideoHlsPlaylistRes>> getHlsPlaylist(
            @RequestBody @Validated VideoHlsPlaylistReq req
    ) {
        Map<String, String> singedCookieMap = videoService.getSingedCookieMap(req);
        String signedUrl = singedCookieMap.get(CLOUDFRONT_SIGNED_URL);

        ResponseCookie cookie1 = ResponseCookie.from(CLOUDFRONT_KEY_PAIR_ID,
                        singedCookieMap.get(CLOUDFRONT_KEY_PAIR_ID))
                .path(signedUrl)
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        ResponseCookie cookie2 = ResponseCookie.from(CLOUDFRONT_SIGNATURE, singedCookieMap.get(CLOUDFRONT_SIGNATURE))
                .path(signedUrl)
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        ResponseCookie cookie3 = ResponseCookie.from(CLOUDFRONT_EXPIRES, singedCookieMap.get(CLOUDFRONT_EXPIRES))
                .path(signedUrl)
                .httpOnly(true)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie1.toString())
                .header(HttpHeaders.SET_COOKIE, cookie2.toString())
                .header(HttpHeaders.SET_COOKIE, cookie3.toString())
                .body(SuccessRes.of(new VideoHlsPlaylistRes(singedCookieMap.get(HLS_MASTER_FILE))));
    }
}
