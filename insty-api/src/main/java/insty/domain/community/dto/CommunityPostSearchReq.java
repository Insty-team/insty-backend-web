package insty.domain.community.dto;

import insty.domain.common.dto.PaginationReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CommunityPostSearchReq(
        @Min(1)
        @Schema(description = "페이지 번호", example = "1", defaultValue = "1")
        int page,

        @Min(1) @Max(100)
        @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
        int pageSize
) {
    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }
}
