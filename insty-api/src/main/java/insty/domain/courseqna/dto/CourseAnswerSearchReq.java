package insty.domain.courseqna.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ParameterObject
public record CourseAnswerSearchReq(

        @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
        @Min(1)
        int page,

        @Schema(description = "페이지당 항목 수 (최대 100)", example = "20", defaultValue = "20")
        @Min(1) @Max(100)
        int pageSize

) {
    public Pageable toPaginationReq() {
        return PageRequest.of(page - 1, pageSize);
    }
}