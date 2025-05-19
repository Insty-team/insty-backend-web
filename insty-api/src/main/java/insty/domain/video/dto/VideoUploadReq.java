package insty.domain.video.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VideoUploadReq(
        @NotBlank
        @Pattern(
                regexp = "^.+\\.[a-zA-Z0-9]+$",
                message = "확장자 명이 포함된 파일 이름이여야 합니다."
        )
        String fileName,
        @NotBlank
        @Pattern(
                regexp = "^video/[a-zA-Z0-9!#$&.+\\\\-^_]+$",
                message = "지원하지 않는 영상 형식입니다."
        )
        String contentType
) {
}
