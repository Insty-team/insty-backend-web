package insty.domain.community.controller;

import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionSearchReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.service.CommunityService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community/")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService CommunityService;

    /// ============================== 질문 API  ======================================

    @Operation(summary = "커뮤니티 질문 검색", description = "강의 목록을 조회한다")
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/search")
    public SuccessRes<SearchRes<CommunityQuestionRes>> searchQuestions(
            @ModelAttribute @Validated CommunityQuestionSearchReq req
    ) {
        return SuccessRes.of(CommunityService.searchQuestions(req));
    }

    @Operation(summary = "강의 영상 별 질문 목록 조회", description = "강의 영상 별 질문 리스트 조회 및 검색 조회")
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/courses/{courseId}")
    public SuccessRes<List<CommunityQuestionRes>> retrieveQuestionsByCourseId(
            @PathVariable @NotBlank Long courseId) {
        return SuccessRes.of(CommunityService.getQuestionsByCourseId(courseId));
    }

    @Operation(summary = "질문 상세 조회", description = "질문 상세 정보 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DETAIL)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/{questionId}")
    public SuccessRes<CommunityQuestionRes> retrieveQuestionDetails(@PathVariable @NotBlank Long questionId) {
        return SuccessRes.of(CommunityService.getQuestionDetails(questionId));
    }

    @Operation(summary = "질문 작성", description = "새로운 질문 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    @PreAuthorize("hasRole('LEARNER')")
    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionRes> createQuestion(
            @RequestPart("communityQuestionReq") @Validated CommunityQuestionCreateReq communityQuestionCreateReq,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(CommunityService.saveQuestion(communityQuestionCreateReq, attachments));
    }

    @Operation(summary = "질문 수정", description = "질문 수정 (첨부파일 업로드 지원)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_UPDATE)
    @PreAuthorize("hasRole('LEARNER')")
    @PatchMapping(value = "/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionRes> updateQuestion(
            @PathVariable @NotBlank Long questionId,
            @RequestPart CommunityQuestionUpdateReq communityQuestionUpdateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 코드 파일 등)")
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(CommunityService.updateQuestion(questionId, communityQuestionUpdateReq, attachments));
    }

    @Operation(summary = "질문 삭제", description = "질문 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DELETE)
    @PreAuthorize("hasRole('LEARNER')")
    @DeleteMapping("/questions/{questionId}")
    public SuccessRes<?> deleteQuestion(@PathVariable @NotBlank Long questionId) {
        CommunityService.deleteQuestion(questionId);
        return SuccessRes.of(null);
    }

    /// ============================== 답변 API  ======================================

    @Operation(summary = "답변 조회", description = "질문에 대한 모든 댓글 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/{questionId}/answer")
    public SuccessRes<List<CommunityAnswerRes>> retrieveAllAnswers(@PathVariable @NotBlank Long questionId) {
        return SuccessRes.of(CommunityService.getAllAnswers(questionId));
    }

    @Operation(summary = "답변 작성", description = "질문에 대한 댓글 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_CREATE)
    @PreAuthorize("hasRole('CREATOR')")
    @PostMapping("/questions/{questionId}/answer")
    public SuccessRes<CommunityAnswerRes> createAnswer(
            @PathVariable @NotBlank Long questionId,
            @RequestPart CommunityAnswerCreateReq communityAnswerCreateReq,
            @Parameter(description = "댓글 이미지 (최대 5개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "answerImages", required = false) @Size(max = 5) List<MultipartFile> imageFiles
    ) {
        
        return SuccessRes.of(CommunityService.saveAnswer(communityAnswerCreateReq, imageFiles));
    }

    @Operation(summary = "답변 수정", description = "질문에 대한 댓글 수정")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_UPDATE)
    @PreAuthorize("hasRole('CREATOR')")
    @PatchMapping(value = "/answer/{answer_id}/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityAnswerRes> updateAnswer(
            @PathVariable @NotBlank Long answerId,
            @RequestPart CommunityAnswerUpdateReq communityAnswerUpdateReq,
            @Parameter(description = "댓글 이미지 (최대 5개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "answerImages", required = false) @Size(max = 5) List<MultipartFile> imageFiles
    ) {
        
        return SuccessRes.of(CommunityService.updateAnswer(answerId, communityAnswerUpdateReq, imageFiles));
    }

    @Operation(summary = "답변 삭제", description = "질문에 대한 댓글 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_DELETE)
    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/answer/{answerId}")
    public SuccessRes<?> deleteAnswer(@PathVariable @NotBlank Long answerId) {
        CommunityService.deleteAnswer(answerId);
        return SuccessRes.of(null);
    }

    /// ============================== 답변 채택 API  ======================================

    @Operation(summary = "답변 채택", description = "질문 작성자가 답변을 채택")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_ACCEPT)
    @PostMapping("/questions/{questionId}/answer/{answerId}/accept")
    public SuccessRes<?> acceptAnswer(@PathVariable @NotBlank Long questionId, @PathVariable @NotBlank Long answerId) {
        CommunityService.acceptAnswer(questionId, answerId);
        return SuccessRes.of(null);
    }


}
