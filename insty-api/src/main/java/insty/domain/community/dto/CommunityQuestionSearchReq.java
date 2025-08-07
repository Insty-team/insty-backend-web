package insty.domain.community.dto;

import insty.domain.common.dto.PaginationReq;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
@Builder
public record CommunityQuestionSearchReq(

        @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
        @Min(1)
        int page,

        @Schema(description = "페이지당 항목 수 (최대 100)", example = "20", defaultValue = "20")
        @Min(1) @Max(100)
        int pageSize,

        @Schema(
                description = "정렬 조건 (필드명:asc|desc, 여러 개는 쉼표로 구분)",
                example = "createdAt:desc",
                defaultValue = "createdAt:desc"
        )
        String sort,

        @Schema(description = "검색 키워드", example = "자바", defaultValue = "")
        String keyword,

        @Schema(description = "답변 여부 필터 (true: 답변 있음, false: 답변 없음)", example = "true", defaultValue = "null")
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
