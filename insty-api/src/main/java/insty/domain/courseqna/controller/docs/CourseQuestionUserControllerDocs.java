package insty.domain.courseqna.controller.docs;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.annotation.Validated;

@Tag(
        name = "내 질문 API",
        description = "사용자가 작성한 모든 강의 Q&A 질문을 조회하는 API 입니다."
)
public interface CourseQuestionUserControllerDocs {

    @Operation(
            summary = "내 모든 질문 목록 검색",
            description = "로그인 사용자가 작성한 모든 강의 Q&A 질문을 페이지네이션하여 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_MY_SEARCH)
    SuccessRes<SearchRes<CourseQuestionMyRes>> searchMyQuestions(
            @CurrentUser @NotNull Long userId,
            @ModelAttribute @Validated CourseQuestionSearchReq req
    );
}
