package insty.domain.community.controller;

import insty.domain.common.SearchRes;
import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionSearchReq;
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

    @Operation(summary = "질문 상세 조회", description = "질문 상세 정보 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DETAIL)
    @GetMapping("/questions/{questionId}")
    public SuccessRes<CommunityQuestionRes> retrieveQuestionDetails(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(CommunityService.getQuestionDetails(questionId));
    }

    @Operation(summary = "강의 영상 별 질문 목록 조회", description = "강의 영상 별 질문 리스트 조회 및 검색 조회")
    @GetMapping("/questions/courses/{courseId}")
    public SuccessRes<List<CommunityQuestionRes>> retrieveQuestionsByCourseId(
            @PathVariable @NotBlank String courseId) {
        return SuccessRes.of(CommunityService.getQuestionsByCourseId(courseId));
    }

    @Operation(summary = "모든 질문 검색", description = "강의 커뮤니티에서 모든 질문 리스트 조회")
    @GetMapping("/questions/search")
    public SuccessRes<List<CommunityQuestionRes>> retrieveAllQuestions() {
        return SuccessRes.of(CommunityService.getAllQuestions());
    }

    @Operation(summary = "질문 작성", description = "새로운 질문 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionRes> createQuestion(
            @RequestPart("communityQuestionReq") @Validated CommunityQuestionReq communityQuestionReq,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(CommunityService.saveQuestion(communityQuestionReq, attachments));
    }

    @Operation(summary = "질문 수정", description = "질문 수정 (첨부파일 업로드 지원)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_UPDATE)
    @PatchMapping(value = "/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionRes> updateQuestion(
            @PathVariable @NotBlank String questionId,
            @RequestPart CommunityQuestionReq communityQuestionReq,
            @Parameter(description = "질문 첨부파일 (이미지, 코드 파일 등)")
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(CommunityService.updateQuestion(communityQuestionReq, attachments));
    }

    @Operation(summary = "질문 삭제", description = "질문 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DELETE)
    @DeleteMapping("/questions/{questionId}")
    public SuccessRes<?> deleteQuestion(@PathVariable @NotBlank String questionId) {
        CommunityService.deleteQuestion(questionId);
        return SuccessRes.of(null);
    }

    @Operation(summary = "커뮤니티 질문 검색", description = "강의 목록을 조회한다")
    @GetMapping("/search")
    public SuccessRes<SearchRes<CommunityQuestionRes>> searchQuestions(
            @ModelAttribute @Validated CommunityQuestionSearchReq req
    ) {
        return SuccessRes.of(CommunityService.searchQuestions(req));
    }

    @Operation(summary = "댓글 조회", description = "질문에 대한 모든 댓글 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_SEARCH)
    @GetMapping("/questions/{questionId}/answer")
    public SuccessRes<List<CommunityAnswerRes>> retrieveAllAnswers(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(CommunityService.getAllAnswers(questionId));
    }

    @Operation(summary = "답변 작성", description = "질문에 대한 댓글 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_CREATE)
    @PostMapping("/questions/{questionId}/answer")
    public SuccessRes<CommunityAnswerRes> createAnswer(
            @PathVariable @NotBlank Long questionId,
            @RequestPart CommunityAnswerReq communityAnswerReq,
            @Parameter(description = "댓글 이미지 (최대 5개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "answerImages", required = false) @Size(max = 5) List<MultipartFile> imageFiles,
            @Parameter(description = "영상 UUID (video 도메인의 업로드 API로 먼저 업로드 후 받은 UUID)")
            @RequestPart(value = "videoUuid", required = false) String videoUuid) {
        
        return SuccessRes.of(CommunityService.saveAnswer(communityAnswerReq, imageFiles, videoUuid));
    }

    @Operation(summary = "답변 수정", description = "질문에 대한 댓글 수정")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_UPDATE)
    @PatchMapping(value = "/questions/{question_id}/answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityAnswerRes> updateAnswer(
            @PathVariable @NotBlank String question_id,
            @RequestPart CommunityAnswerReq communityAnswerReq,
            @Parameter(description = "댓글 이미지 (최대 5개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "answerImages", required = false) @Size(max = 5) List<MultipartFile> imageFiles,
            @Parameter(description = "영상 UUID (video 도메인의 업로드 API로 먼저 업로드 후 받은 UUID)")
            @RequestPart(value = "videoUuid", required = false) String videoUuid) {
        
        return SuccessRes.of(CommunityService.updateAnswer(communityAnswerReq, imageFiles, videoUuid));
    }

    @Operation(summary = "답변 삭제", description = "질문에 대한 댓글 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_DELETE)
    @DeleteMapping("/questions/{question_id}/answer/{answerId}")
    public SuccessRes<?> deleteAnswer(@PathVariable @NotBlank String question_id, @PathVariable @NotBlank String answerId) {
        CommunityService.deleteAnswer(answerId);
        return SuccessRes.of(null);
    }

    @Operation(summary = "답변 채택", description = "질문 작성자가 답변을 채택")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_ACCEPT)
    @PostMapping("/questions/{questionId}/answer/{answerId}/accept")
    public SuccessRes<?> acceptAnswer(@PathVariable @NotBlank String questionId, @PathVariable @NotBlank String answerId) {
        CommunityService.acceptAnswer(questionId, answerId);
        return SuccessRes.of(null);
    }

    @Operation(summary = "답변 채택 해제", description = "질문 작성자가 채택된 답변을 해제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_UNACCEPT)
    @DeleteMapping("/questions/{questionId}/answer/accept")
    public SuccessRes<?> unacceptAnswer(@PathVariable @NotBlank String questionId) {
        CommunityService.unacceptAnswer(questionId);
        return SuccessRes.of(null);
    }

}
