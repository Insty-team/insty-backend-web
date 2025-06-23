package insty.domain.course.dto;

import insty.domain.common.dto.PaginationReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CourseMySearchReq(
        @Min(1)
        int page,
        @Min(1) @Max(100)
        int pageSize
) {

    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }
}
