package insty.domain.community.controller;

import static insty.domain.common.AttachmentConstraints.MAX_POST_FILE_COUNT;
import static insty.domain.common.AttachmentConstraints.MAX_COMMENT_FILE_COUNT;
import static insty.domain.common.AttachmentConstraints.POST_ATTACHMENT_DESCRIPTION;
import static insty.domain.common.AttachmentConstraints.COMMENT_ATTACHMENT_DESCRIPTION;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.dto.CourseAnswerCreateReq;
import insty.domain.courseqna.dto.CourseAnswerRes;
import insty.domain.courseqna.dto.CourseAnswerSearchReq;
import insty.domain.courseqna.dto.CourseAnswerUpdateReq;
import insty.domain.courseqna.dto.CourseQnaAcceptAnswerResultRes;
import insty.domain.courseqna.dto.CourseQuestionCreateReq;
import insty.domain.courseqna.dto.CourseQuestionDetailsRes;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionRes;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.domain.courseqna.dto.CourseQuestionUpdateReq;
import insty.domain.common.dto.PaginationRes;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "강좌/Q&A API (Deprecated)",
        description = "더 이상 사용되지 않는 강좌 Q&A API, 더미 응답을 반환합니다."
)
@Deprecated
@RestController
@RequestMapping("/api/v1/community/")
@RequiredArgsConstructor
public class CommunityQnaController {

    /// ============================== 질문 API  ======================================

    @GetMapping("/questions")
    public SuccessRes<SearchRes<CourseQuestionRes>> searchQuestions(
            @ModelAttribute @Validated CourseQuestionSearchReq req
    ) {
        PaginationRes pagination = PaginationRes.of(0, req.page(), req.pageSize());
        return SuccessRes.of(SearchRes.from(pagination, List.of()));
    }

    @GetMapping("/questions/course/{courseId}")
    public SuccessRes<SearchRes<CourseQuestionRes>> searchQuestionsByCourse(
            @PathVariable @NotNull Long courseId,
            @ModelAttribute @Validated CourseQuestionSearchReq req
    ) {
        PaginationRes pagination = PaginationRes.of(0, req.page(), req.pageSize());
        return SuccessRes.of(SearchRes.from(pagination, List.of()));
    }

    @GetMapping("/questions/my")
    public SuccessRes<SearchRes<CourseQuestionMyRes>> searchQuestionsByUser(
            @CurrentUser Long userId,
            @ModelAttribute @Validated CourseQuestionSearchReq req
    ) {
        PaginationRes pagination = PaginationRes.of(0, req.page(), req.pageSize());
        return SuccessRes.of(SearchRes.from(pagination, List.of()));
    }

    @GetMapping("/questions/{questionId}")
    public SuccessRes<CourseQuestionDetailsRes> getQuestionDetails(
            @PathVariable @NotNull Long questionId,
            @CurrentUser Long userId
    ) {
        return SuccessRes.of(null);
    }

    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseQuestionDetailsRes> createQuestion(
            @CurrentUser Long userId,
            @RequestPart("communityQuestionReq") @Validated CourseQuestionCreateReq courseQuestionCreateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_POST_FILE_COUNT) @Schema(description = POST_ATTACHMENT_DESCRIPTION) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(null);
    }

    @PatchMapping(value = "/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseQuestionDetailsRes> updateQuestion(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseQuestionUpdateReq courseQuestionUpdateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_POST_FILE_COUNT) @Schema(description = POST_ATTACHMENT_DESCRIPTION) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(null);
    }

    @DeleteMapping("/questions/{questionId}")
    public SuccessRes<?> deleteQuestion(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId
    ) {
        return SuccessRes.of(null);
    }

    /// ============================== 답변 API  ======================================

    @GetMapping("/questions/{questionId}/answer")
    public SuccessRes<SearchRes<CourseAnswerRes>> retrieveAnswers(
            @PathVariable @NotNull Long questionId,
            @ModelAttribute @Validated CourseAnswerSearchReq req
    ) {
        PaginationRes pagination = PaginationRes.of(0, req.page(), req.pageSize());
        return SuccessRes.of(SearchRes.from(pagination, List.of()));
    }

    @GetMapping("/questions/{questionId}/answer/accepted")
    public SuccessRes<List<CourseAnswerRes>> getAcceptedAnswers(
            @PathVariable @NotNull Long questionId
    ) {
        return SuccessRes.of(List.of());
    }

    @PostMapping(value = "/questions/{questionId}/answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseAnswerRes> createAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseAnswerCreateReq COurseAnswerCreateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_COMMENT_FILE_COUNT) @Schema(description = COMMENT_ATTACHMENT_DESCRIPTION) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(null);
    }

    @PatchMapping(value = "/answer/{answerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseAnswerRes> updateAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long answerId,
            @RequestPart CourseAnswerUpdateReq courseAnswerUpdateReq,
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_COMMENT_FILE_COUNT) @Schema(description = COMMENT_ATTACHMENT_DESCRIPTION) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(null);
    }

    @DeleteMapping("/answer/{answerId}")
    public SuccessRes<?> deleteAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long answerId
    ) {
        return SuccessRes.of(null);
    }

    /// ============================== 답변 채택 API  ======================================

    @PostMapping("/questions/{questionId}/answer/{answerId}/accept")
    public SuccessRes<CourseQnaAcceptAnswerResultRes> acceptAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    ) {
        return SuccessRes.of(new CourseQnaAcceptAnswerResultRes(null, false));
    }

}
