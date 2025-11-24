package insty.domain.course.dto;

import insty.domain.common.dto.PaginationReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CourseMySearchReq(
        @Min(1)
        int page,
        @Min(1) @Max(100)
        int pageSize,
        Boolean isShow
) {

    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }
}
