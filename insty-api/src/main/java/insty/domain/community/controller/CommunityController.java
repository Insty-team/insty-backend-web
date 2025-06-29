package insty.domain.community.controller;

import insty.domain.community.dto.CommunityAnswerReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityQuestionReq;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.service.CommunityService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
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

    private final CommunityService communityService;

    @Operation(summary = "질문 상세 조회", description = "질문 상세 정보 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DETAIL)
    @GetMapping("/questions/{question_id}")
    public SuccessRes<CommunityQuestionRes> retrieveQuestionDetails(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(communityService.getQuestionDetails(questionId));
    }

    @Operation(summary = "강의 영상 별 질문 목록 조회", description = "강의 영상 별 질문 리스트 조회 및 검색 조회")
    @GetMapping("/questions/courses/{course_id}")
    public SuccessRes<List<CommunityQuestionRes>> retrieveQuestionsByCourseId(
            @PathVariable @NotBlank String courseId) {
        return SuccessRes.of(communityService.getQuestionsByCourseId(courseId));
    }

    @Operation(summary = "모든 질문 검색", description = "강의 커뮤니티에서 모든 질문 리스트 조회")
    @GetMapping("/questions/search")
    public SuccessRes<List<CommunityQuestionRes>> retrieveAllQuestions() {
        return SuccessRes.of(communityService.getAllQuestions());
    }

    @Operation(summary = "질문 작성", description = "새로운 질문 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionRes> createQuestion(
            @RequestPart("communityQuestionReq") @Validated CommunityQuestionReq communityQuestionReq,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityService.saveQuestion(communityQuestionReq, attachments));
    }

    @Operation(summary = "질문 수정", description = "질문 수정")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_UPDATE)
    @PatchMapping("/questions/{question_id}")
    public SuccessRes<CommunityQuestionRes> updateQuestion(
            @PathVariable @NotBlank String questionId,
            @RequestPart CommunityQuestionReq communityQuestionReq,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        //communityQuestionReq.setId(questionId);
        return SuccessRes.of(communityService.updateQuestion(communityQuestionReq, attachments));
    }

    @Operation(summary = "질문 삭제", description = "질문 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DELETE)
    @DeleteMapping("/questions/{question_id}")
    public SuccessRes<?> deleteQuestion(@PathVariable @NotBlank String questionId) {
        communityService.deleteQuestion(questionId);
        return SuccessRes.of(null);
    }

    //삭제하고 질문 상세보기와 통합
    @Operation(summary = "댓글 조회", description = "질문에 대한 모든 댓글 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_SEARCH)
    @GetMapping("/questions/{question_id}/answer")
    public SuccessRes<List<CommunityAnswerRes>> retrieveAllAnswers(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(communityService.getAllAnswers(questionId));
    }

    @Operation(summary = "답변 작성", description = "질문에 대한 댓글 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_CREATE)
    @PostMapping("/questions/{question_id}/answer")
    public SuccessRes<CommunityAnswerRes> createAnswer(
            @RequestPart CommunityAnswerReq communityAnswerReq,
            @RequestPart(value = "answerImage", required = false) MultipartFile imageFile){
        return SuccessRes.of(communityService.saveAnswer(communityAnswerReq, imageFile));
    }

    @Operation(summary = "답변 수정", description = "질문에 대한 댓글 수정")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_UPDATE)
    @PatchMapping("/questions/{question_id}/answer")
    public SuccessRes<CommunityAnswerRes> updateAnswer( @RequestBody CommunityAnswerReq communityQuestionReq) {
        return SuccessRes.of(communityService.updateAnswer(communityQuestionReq));
    }

    @Operation(summary = "답변 삭제", description = "질문에 대한 댓글 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_DELETE)
    @DeleteMapping("/questions/{question_id}/answer")
    public SuccessRes<?> deleteAnswer(@RequestBody CommunityAnswerReq communityAnswerReq) {
        return SuccessRes.of(null);
    }

    @Operation(summary = "답변 이미지 업로드", description = "댓글에 이미지 업로드")
    //@CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_IMAGE_UPLOAD)
    @PostMapping("/questions/{question_id}/answer/image")
    public SuccessRes<CommunityAnswerRes> postAnswerImage(@RequestBody CommunityAnswerReq communityAnswerReq) {
        return SuccessRes.of(communityService.postAnswerImage(communityAnswerReq));
    }

    @Operation(summary = "답변 비디오 업로드", description = "댓글에 비디오 업로드")
    //@CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_VIDEO_UPLOAD)
    @PostMapping("/questions/{question_id}/answer/video")
    public SuccessRes<CommunityAnswerReq> postAnswerVideo(@RequestBody CommunityAnswerReq communityAnswerReq) {
        return SuccessRes.of(communityService.postAnswerVideo(communityAnswerReq));
    }

}
