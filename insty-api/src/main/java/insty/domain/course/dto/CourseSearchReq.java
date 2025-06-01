package insty.domain.course.dto;

import jakarta.validation.constraints.Min;

public record CourseSearchReq(
        @Min(1)
        int page,
        @Min(1)
        int pageSize,
        String search
) {

    public long getOffset() {
        return (long) (page - 1) * pageSize;
    }
}
