package insty.domain.community.dto;

import insty.domain.common.dto.PaginationReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CommunityQuestionSearchReq(
        @Min(1) int page,
        @Min(1) @Max(100) int pageSize,
        Long courseId,
        Boolean isAnswered,
        String keyword,
        String sort
) {
    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }
    public CommunityQuestionSearchFilter toSearchFilter() {
        return new CommunityQuestionSearchFilter(courseId, isAnswered, keyword);
    }
}