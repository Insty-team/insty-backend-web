package insty.domain.community.dto;

import insty.domain.common.dto.PaginationReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
@Builder
public record CommunityQuestionSearchReq(
        @Min(1) int page,
        @Min(1) @Max(100) int pageSize,
        String sort,
        //String search, 키워드로 대체
        String keyword,
        Boolean isAnswered
) {
    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }
    public CommunityQuestionSearchFilter toSearchFilter() {
        return new CommunityQuestionSearchFilter(keyword, null, isAnswered, null, null);
    }
    public CommunityQuestionSearchFilter toSearchFilterWithUser(Long userId) {
        return new CommunityQuestionSearchFilter(keyword, null, isAnswered, null, userId);
    }

    public CommunityQuestionSearchFilter toSearchFilterWithCourseId(Long courseId) {
        return new CommunityQuestionSearchFilter(keyword, null, isAnswered, courseId, null);
    }
}