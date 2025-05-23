package insty.domain.video.controller;

import insty.domain.video.dto.VideoUploadReq;
import insty.domain.video.dto.VideoUploadRes;
import insty.domain.video.service.VideoService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public SuccessRes<VideoUploadRes> upload(
            @RequestBody @Validated VideoUploadReq req
    ) {
        return SuccessRes.of(videoService.getPreSignedURLForUpload(req));
    }
}
