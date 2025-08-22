package insty.domain.mention.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
@Builder
public record MentionUserSearchReq(
        @Schema(description = "검색 키워드", example = "홍길동")
        @NotBlank(message = "검색 키워드를 반드시 입력해야 합니다")
        @Size(max = 30, message = "검색 키워드는 최대 30자까지 가능합니다")
        String search,

        @Schema(description = "조회할 개수 (1~100)", example = "10", defaultValue = "10")
        @Min(1) @Max(100)
        int size
) {
    public MentionUserSearchReq {
        if (size == 0) size = 10;
    }
}
