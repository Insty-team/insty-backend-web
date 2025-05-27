package insty.domain.video.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VideoUploadReq(
        @Schema(description = "파일명(mp4,mov,webm)", example = "fileName.mp4")
        @NotBlank
        @Pattern(
                regexp = "^.+\\.[a-zA-Z0-9]+$",
                message = "확장자 명이 포함된 파일 이름이여야 합니다."
        )
        String fileName,
        @Schema(description = "영상 타입(video/mp4,video/quicktime,video/webm)", example = "video/mp4")
        @NotBlank
        @Pattern(
                regexp = "^video/[a-zA-Z0-9!#$&.+\\\\-^_]+$",
                message = "지원하지 않는 영상 형식입니다."
        )
        String contentType
) {
}
