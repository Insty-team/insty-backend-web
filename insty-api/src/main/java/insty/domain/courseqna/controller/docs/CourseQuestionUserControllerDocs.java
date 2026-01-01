package insty.domain.courseqna.controller.docs;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(
        name = "내 질문 API",
        description = "사용자가 작성한 모든 강의 Q&A 질문을 조회하는 API 입니다."
)
public interface CourseQuestionUserControllerDocs {

    /**
     * Retrieve a paginated list of course Q&A questions authored by the current user.
     *
     * @param userId the authenticated user's ID
     * @param req    search and pagination criteria for filtering the user's questions
     * @return       a SuccessRes containing a SearchRes of CourseQuestionMyRes with the paginated results
     */
    @Operation(
            summary = "내 모든 질문 목록 검색",
            description = "로그인 사용자가 작성한 모든 강의 Q&A 질문을 페이지네이션하여 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_MY_SEARCH)
    SuccessRes<SearchRes<CourseQuestionMyRes>> searchMyQuestions(
            @CurrentUser Long userId,
            @ModelAttribute CourseQuestionSearchReq req
    );
}