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

    @Operation(summary = "질문 목록 검색 (강의별)", description = "해당 강의의 질문 목록을 조회한다. (제목/내용 키워드 검색)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_SEARCH)
    SuccessRes<SearchRes<CourseQuestionRes>> searchQuestions(
            @PathVariable @NotNull Long courseId,
            @ModelAttribute CourseQuestionSearchReq req
    );

    @Operation(summary = "내가 작성한 질문 검색 (강의별)", description = "해당 강의에서 러너 자신이 작성한 질문 목록을 조회한다. (인증 사용자 기준)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_MY_SEARCH)
    SuccessRes<SearchRes<CourseQuestionMyRes>> searchQuestionsByUser(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @ModelAttribute CourseQuestionSearchReq req
    );

    @Operation(summary = "질문 상세 조회", description = "질문의 상세 정보를 조회한다. (답변 최신순)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DETAIL)
    SuccessRes<CourseQuestionDetailsRes> getQuestionDetails(
            @PathVariable @NotNull Long courseId,
            @PathVariable @NotNull Long questionId,
            @CurrentUser Long userId
    );

    @Operation(summary = "질문 작성", description = "질문을 생성한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    SuccessRes<CourseQuestionDetailsRes> createQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @RequestPart("courseQuestionReq") CourseQuestionCreateReq courseQuestionCreateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 최대 2개)", content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    );

    @Operation(summary = "질문 수정", description = "질문을 수정한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_UPDATE)
    SuccessRes<CourseQuestionDetailsRes> updateQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CourseQuestionUpdateReq courseQuestionUpdateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 최대 2개)", content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    );

    @Operation(summary = "질문 삭제", description = "질문을 삭제한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DELETE)
    SuccessRes<?> deleteQuestion(
            @PathVariable @NotNull Long courseId,
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId
    );
}
