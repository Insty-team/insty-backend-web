package insty.domain.mention.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
@Builder
public record MentionUserSearchReq(
        @Schema(description = "검색 키워드", example = "홍길동")
        String keyword,

        @Schema(description = "조회할 개수 (1~100)", example = "10", defaultValue = "10")
        @Min(1) @Max(100)
        int size
) {
}
