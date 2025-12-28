package insty.domain.courseqna.controller.docs;

import insty.domain.common.SearchRes;
import insty.domain.courseqna.dto.CourseQuestionCreateReq;
import insty.domain.courseqna.dto.CourseQuestionDetailsRes;
import insty.domain.courseqna.dto.CourseQuestionMyRes;
import insty.domain.courseqna.dto.CourseQuestionRes;
import insty.domain.courseqna.dto.CourseQuestionSearchReq;
import insty.domain.courseqna.dto.CourseQuestionUpdateReq;
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
        name = "강의 Q&A 질문 API",
        description = "강의별 Q&A 질문을 관리하는 API입니다."
)
public interface CourseQuestionControllerDocs {

    @Operation(
            summary = "강의 질문 목록 검색",
            description = "특정 강의에 등록된 질문을 최신순/정렬 기준 및 키워드/상태 필터로 페이지네이션 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_SEARCH)
    SuccessRes<SearchRes<CourseQuestionRes>> searchQuestions(
            @PathVariable @NotNull Long courseId,
            @ModelAttribute CourseQuestionSearchReq req
    );

    @Operation(
            summary = "내 질문 목록 검색",
            description = "로그인 사용자가 해당 강의에서 작성한 질문만 필터링해 페이지네이션 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_MY_SEARCH)
    SuccessRes<SearchRes<CourseQuestionMyRes>> searchQuestionsByUser(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @ModelAttribute CourseQuestionSearchReq req
    );

    @Operation(
            summary = "질문 상세 조회",
            description = "질문의 본문, 상태, 첨부파일, 비디오 정보와 최신순 답변 요약을 조회합니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_DETAIL)
    SuccessRes<CourseQuestionDetailsRes> getQuestionDetails(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId,
            @CurrentUser Long userId
    );

    @Operation(
            summary = "질문 작성",
            description = "제목/내용/첨부파일(최대 2개)과 비디오 UUID를 포함해 새로운 질문을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "질문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 유효성 검증 실패"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_CREATE)
    SuccessRes<CourseQuestionDetailsRes> createQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @RequestPart("courseQuestionReq") CourseQuestionCreateReq courseQuestionCreateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 최대 2개)", content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    );

    @Operation(
            summary = "질문 수정",
            description = "기존 질문의 제목/내용/첨부파일/비디오를 수정합니다. 삭제 파일 ID와 신규 첨부를 함께 전송할 수 있습니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_UPDATE)
    SuccessRes<CourseQuestionDetailsRes> updateQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseQuestionUpdateReq courseQuestionUpdateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 최대 2개)", content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    );

    @Operation(
            summary = "질문 삭제",
            description = "본인이 작성한 질문을 삭제합니다. 연결된 답변/파일/비디오도 함께 정리됩니다."
    )
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_QUESTION_DELETE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "작성자 아님"),
            @ApiResponse(responseCode = "404", description = "질문 없음")
    })
    void deleteQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId
    );
}
