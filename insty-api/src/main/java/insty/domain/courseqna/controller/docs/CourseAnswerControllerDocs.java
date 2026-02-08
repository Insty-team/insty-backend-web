package insty.domain.courseqna.controller.docs;

import static insty.domain.common.AttachmentConstraints.MAX_QNA_FILE_COUNT;
import static insty.domain.common.AttachmentConstraints.QNA_ATTACHMENT_DESCRIPTION;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "답변 목록 조회",
            description = "질문에 등록된 답변을 최신순/페이지네이션으로 조회합니다. 비디오/파일 정보를 함께 반환합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_SEARCH)
    SuccessRes<SearchRes<CourseAnswerRes>> retrieveAnswers(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId,
            @ModelAttribute CourseAnswerSearchReq req
    );

    @Operation(
            summary = "채택된 답변 조회",
            description = "질문에서 채택된 답변만 조회합니다. 없으면 빈 배열을 반환합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_ACCEPTED_SEARCH)
    SuccessRes<List<CourseAnswerRes>> getAcceptedAnswers(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId
    );

    @Operation(
            summary = "답변 작성",
            description = "질문에 대한 답변을 작성합니다. 본문과 선택적 첨부파일, 비디오 UUID를 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "답변 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_CREATE)
    SuccessRes<CourseAnswerRes> createAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseAnswerCreateReq courseAnswerCreateReq,
            @Parameter(description = QNA_ATTACHMENT_DESCRIPTION, content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_QNA_FILE_COUNT) List<MultipartFile> attachments
    );

    @Operation(
            summary = "답변 수정",
            description = "기존 답변의 본문/첨부파일/비디오를 수정합니다. 삭제 파일 ID와 신규 첨부를 함께 전송할 수 있습니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_UPDATE)
    SuccessRes<CourseAnswerRes> updateAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId,
            @RequestPart CourseAnswerUpdateReq courseAnswerUpdateReq,
            @Parameter(description = QNA_ATTACHMENT_DESCRIPTION, content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = MAX_QNA_FILE_COUNT) List<MultipartFile> attachments
    );

    @Operation(
            summary = "답변 삭제",
            description = "본인이 작성한 답변을 삭제합니다. 연결된 파일/비디오도 함께 정리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "작성자 아님"),
            @ApiResponse(responseCode = "404", description = "답변 없음")
    })
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_DELETE)
    void deleteAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    );

    @Operation(
            summary = "답변 채택",
            description = "질문자가 특정 답변을 채택/해제합니다. 한 질문에는 하나의 답변만 채택됩니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_ANSWER_ACCEPT)
    SuccessRes<CourseQnaAcceptAnswerResultRes> acceptAnswer(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    );
}
