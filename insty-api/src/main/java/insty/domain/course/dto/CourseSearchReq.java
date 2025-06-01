package insty.domain.course.dto;

import insty.domain.common.dto.PaginationReq;
import jakarta.validation.constraints.Min;

public record CourseSearchReq(
        @Min(1)
        int page,
        @Min(1)
        int pageSize,
        String search
) {

    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }

    public CourseSearchFilter toSearchFilter() {
        return new CourseSearchFilter(search);
    }
}
