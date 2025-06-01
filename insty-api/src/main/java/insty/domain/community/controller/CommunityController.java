package insty.domain.community.controller;

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
    public SuccessRes<?> createAnswer(@RequestBody CommunityQuestionReq communityQuestionReq) {
        return SuccessRes.of(null);
    }
}
