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
import org.springframework.web.bind.annotation.*;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community/")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @Operation(summary = "질문 상세 조회", description = "질문 상세 정보 조회")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    @GetMapping("/questions/{question_id}")
    public SuccessRes<CommunityQuestionRes> retrieveQuestionDetails(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(communityService.getQuestionDetails(questionId));
    }

    @Operation(summary = "답변 작성", description = "질문에 대한 댓글 작성")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_CREATE)
    @PostMapping("/questions/{question_id}/answer")
    public SuccessRes<CommunityAnswerRes> createAnswer(@RequestBody CommunityAnswerReq communityAnswerReq) {
        return SuccessRes.of(communityService.saveAnswer(communityAnswerReq));
    }

    @Operation(summary = "답변 수정", description = "질문에 대한 댓글 수정")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_UPDATE)
    @PatchMapping("/questions/{question_id}/answer")
    public SuccessRes<?> updateAnswer( @RequestBody CommunityAnswerReq communityQuestionReq) {
        return SuccessRes.of(null);
    }

    @Operation(summary = "답변 삭제", description = "질문에 대한 댓글 삭제")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_DELETE)
    @DeleteMapping("/questions/{question_id}/answer")
    public SuccessRes<?> deleteAnswer(@RequestBody CommunityAnswerReq communityAnswerReq) {
        return SuccessRes.of(null);
    }

    @Operation(summary = "AI 답변 추천", description = "AI 응답 추천 받기")
    //@CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_AI_RECOMMENDATION)
    @PostMapping("/questions/{question_id}/answer/ai")
    public SuccessRes<CommunityAnswerRes> getAIAnswerRecommendation(@RequestBody CommunityAnswerReq communityAnswerReq) {
        return SuccessRes.of(communityService.getAIAnswerRecommendation(communityAnswerReq));
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

    @Operation(summary = "질문 작성", description = "새로운 질문 작성")
    //@CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    @PostMapping("/questions")
    public SuccessRes<CommunityQuestionRes> createQuestion(@RequestBody CommunityQuestionReq communityQuestionReq) {
        return SuccessRes.of(communityService.saveQuestion(communityQuestionReq));
    }
}
