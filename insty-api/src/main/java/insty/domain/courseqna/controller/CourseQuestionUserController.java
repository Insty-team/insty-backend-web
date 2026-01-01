package insty.domain.courseqna.controller;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.controller.docs.CourseQuestionUserControllerDocs;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.domain.courseqna.service.CourseQuestionService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class CourseQuestionUserController implements CourseQuestionUserControllerDocs {

    private final CourseQuestionService courseQuestionService;

    /**
     * Retrieve the authenticated learner's course questions matching the given search criteria.
     *
     * @param userId the ID of the currently authenticated user
     * @param req    search and pagination criteria used to filter the user's questions
     * @return       a SuccessRes wrapping a SearchRes of CourseQuestionMyRes containing the matching questions
     */
    @PreAuthorize("hasRole('LEARNER')")
    @GetMapping("/me")
    public SuccessRes<SearchRes<CourseQuestionMyRes>> searchMyQuestions(
            @CurrentUser @NotNull Long userId,
            @ModelAttribute @Validated CourseQuestionSearchReq req
    ) {
        return SuccessRes.of(courseQuestionService.searchQuestionsByUserId(req, userId));
    }
}