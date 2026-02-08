package insty.domain.courseqna.controller;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.controller.docs.CourseQuestionControllerDocs;
import insty.domain.courseqna.dto.CourseQuestionCreateReq;
import insty.domain.courseqna.dto.CourseQuestionDetailsRes;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionRes;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.domain.courseqna.dto.CourseQuestionUpdateReq;
import insty.domain.courseqna.service.CourseQuestionService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class CourseQuestionController implements CourseQuestionControllerDocs {

    private final CourseQuestionService courseQuestionService;

    @GetMapping
    public SuccessRes<SearchRes<CourseQuestionRes>> searchQuestions(
            @PathVariable @NotNull Long courseId,
            @ModelAttribute @Validated CourseQuestionSearchReq req
    ) {
        return SuccessRes.of(courseQuestionService.searchQuestionsByCourseId(req, courseId));
    }

    @GetMapping("/me")
    public SuccessRes<SearchRes<CourseQuestionMyRes>> searchQuestionsByUser(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @ModelAttribute @Validated CourseQuestionSearchReq req
    ) {
        return SuccessRes.of(courseQuestionService.searchQuestionsByUserId(req, userId, courseId));
    }

    @GetMapping("/{questionId}")
    public SuccessRes<CourseQuestionDetailsRes> getQuestionDetails(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId,
            @CurrentUser Long userId
    ) {
        CourseQuestionDetailsRes response = courseQuestionService.getQuestionDetails(questionId, userId);
        return SuccessRes.of(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessRes<CourseQuestionDetailsRes> createQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @RequestPart("courseQuestionReq") @Validated CourseQuestionCreateReq courseQuestionCreateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = insty.domain.common.AttachmentConstraints.MAX_QNA_FILE_COUNT) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(courseQuestionService.saveQuestion(userId, courseId, courseQuestionCreateReq, attachments));
    }

    @PatchMapping(value = "/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseQuestionDetailsRes> updateQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseQuestionUpdateReq courseQuestionUpdateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = insty.domain.common.AttachmentConstraints.MAX_QNA_FILE_COUNT) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(
                courseQuestionService.updateQuestion(userId, questionId, courseQuestionUpdateReq, attachments));
    }

    @DeleteMapping("/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId
    ) {
        courseQuestionService.deleteQuestion(userId, questionId);
    }
}
