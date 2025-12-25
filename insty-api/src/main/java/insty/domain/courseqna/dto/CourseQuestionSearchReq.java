package insty.domain.courseqna.dto;

import insty.domain.common.dto.PaginationReq;
import insty.model.courseqna.CommunityBoardType;
import insty.model.courseqna.QuestionStatus;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CourseQuestionSearchReq(

        @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
        @Min(1)
        int page,

        @Schema(description = "페이지당 항목 수 (최대 100)", example = "20", defaultValue = "20")
        @Min(1) @Max(100)
        int pageSize,

        @Schema(description = "정렬 기준 필드", example = "createdAt", defaultValue = "createdAt")
        String orderBy,

        @Schema(description = "정렬 방향", example = "desc", defaultValue = "desc", allowableValues = {"asc", "desc"})
        @Pattern(regexp = "(?i)asc|desc", message = "정렬 방향은 asc 또는 desc 이어야 합니다.")
        String order,

        @Schema(description = "검색 키워드", example = "자바", defaultValue = "")
        String keyword,

        @Schema(description = "질문 상태 필터 (다중 선택)", example = "[\"WAITING\", \"ANSWERED\"]")
        List<QuestionStatus> statuses,

        @Schema(description = "게시판 타입(QA / COMMUNITY)", example = "QNA")
        CommunityBoardType boardType
) {
    public PaginationReq toPaginationReq() {
        return new PaginationReq(page, pageSize);
    }

    public String orderByClause() {
        String field = (orderBy == null || orderBy.isBlank()) ? "createdAt" : orderBy;
        String dir = (order == null || order.isBlank()) ? "desc" : order.toLowerCase();
        return field + ":" + dir;
    }

    /**
     * 필터 객체로 변환
     */
    public CourseQuestionSearchFilter toFilter(Long userId, Long courseId) {
        return new CourseQuestionSearchFilter(
                this.keyword,
                this.statuses,
                courseId,
                userId,
                this.boardType
        );
    }
}
