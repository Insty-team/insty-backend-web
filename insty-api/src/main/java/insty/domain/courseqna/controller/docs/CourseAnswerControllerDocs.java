package insty.domain.courseqna.controller.docs;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.dto.CourseAnswerCreateReq;
import insty.domain.courseqna.dto.CourseAnswerRes;
import insty.domain.courseqna.dto.CourseAnswerSearchReq;
import insty.domain.courseqna.dto.CourseAnswerUpdateReq;
import insty.domain.courseqna.dto.CourseQnaAcceptAnswerResultRes;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(
        name = "강의 Q&A 답변 API",
        description = "강의별 Q&A 답변을 관리하는 API입니다."
)
public interface CourseAnswerControllerDocs {

    @Operation(summary = "답변 조회", description = "질문의 답변 목록을 페이지네이션으로 조회한다. (최신순)")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_SEARCH)
    SuccessRes<SearchRes<CourseAnswerRes>> retrieveAnswers(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId,
            @ModelAttribute CourseAnswerSearchReq req
    );

    @Operation(summary = "채택된 답변 조회", description = "질문에서 채택된 답변을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_ACCEPTED_SEARCH)
    SuccessRes<List<CourseAnswerRes>> getAcceptedAnswers(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId
    );

    @Operation(summary = "답변 작성", description = "질문에 답변을 생성한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_CREATE)
    SuccessRes<CourseAnswerRes> createAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseAnswerCreateReq courseAnswerCreateReq,
            @Parameter(description = "답변 첨부파일 (이미지, 최대 1개)", content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    );

    @Operation(summary = "답변 수정", description = "답변을 수정한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_UPDATE)
    SuccessRes<CourseAnswerRes> updateAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId,
            @RequestPart CourseAnswerUpdateReq courseAnswerUpdateReq,
            @Parameter(description = "답변 첨부파일 (이미지, 최대 1개)", content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    );

    @Operation(summary = "답변 삭제", description = "답변을 삭제한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_DELETE)
    SuccessRes<?> deleteAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    );

    @Operation(summary = "답변 채택", description = "질문자가 답변을 채택/해제한다. (토글)")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_ACCEPT)
    SuccessRes<CourseQnaAcceptAnswerResultRes> acceptAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    );
}
