package insty.domain.course.dto;

import insty.domain.common.dto.PaginationReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CourseMySearchReq(
        @Min(1)
        int page,

        @Min(1)
        @Max(100)
        int pageSize,

        Boolean isShow,

        @Size(max = 100)
        String title,

        CourseMyCourseSortType sortType
) {

    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }

    public CourseMySearchFilter toCourseMySearchFilter() {
        return new CourseMySearchFilter(title, isShow, sortType);
    }
}
