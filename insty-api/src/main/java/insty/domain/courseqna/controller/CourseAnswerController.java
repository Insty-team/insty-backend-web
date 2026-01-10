package insty.domain.courseqna.controller;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.controller.docs.CourseAnswerControllerDocs;
import insty.domain.courseqna.dto.CourseAnswerCreateReq;
import insty.domain.courseqna.dto.CourseAnswerRes;
import insty.domain.courseqna.dto.CourseAnswerSearchReq;
import insty.domain.courseqna.dto.CourseAnswerUpdateReq;
import insty.domain.courseqna.dto.CourseQnaAcceptAnswerResultRes;
import insty.domain.courseqna.service.CourseAnswerService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/questions")
@RequiredArgsConstructor
public class CourseAnswerController implements CourseAnswerControllerDocs {

    private final CourseAnswerService courseAnswerService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{questionId}/answers")
    public SuccessRes<SearchRes<CourseAnswerRes>> retrieveAnswers(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId,
            @ModelAttribute @Validated CourseAnswerSearchReq req
    ) {
        return SuccessRes.of(courseAnswerService.getAnswersByQuestionId(questionId, req));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{questionId}/answers/accepted")
    public SuccessRes<List<CourseAnswerRes>> getAcceptedAnswers(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId
    ) {
        return SuccessRes.of(courseAnswerService.getAcceptedAnswers(questionId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/{questionId}/answers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessRes<CourseAnswerRes> createAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseAnswerCreateReq courseAnswerCreateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(courseAnswerService.saveAnswer(userId, questionId, courseAnswerCreateReq, attachments));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/{questionId}/answers/{answerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseAnswerRes> updateAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId,
            @RequestPart CourseAnswerUpdateReq courseAnswerUpdateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(courseAnswerService.updateAnswer(userId, answerId, courseAnswerUpdateReq, attachments));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{questionId}/answers/{answerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    ) {
        courseAnswerService.deleteAnswer(userId, answerId);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{questionId}/answers/{answerId}/accept")
    public SuccessRes<CourseQnaAcceptAnswerResultRes> acceptAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    ) {
        return SuccessRes.of(courseAnswerService.acceptAnswer(userId, questionId, answerId));
    }
}
