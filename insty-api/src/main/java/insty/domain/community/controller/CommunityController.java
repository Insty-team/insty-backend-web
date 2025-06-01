package insty.domain.community.controller;

import insty.domain.community.service.CommunityService;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community/")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/questions/{question_id}")
    public SuccessRes<?> retrieveQuestionDetails(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(null);
    }

    @PostMapping("/questions/{question_id}/answer")
    public SuccessRes<?> createAnswer(@PathVariable @NotBlank String questionId) {
        return SuccessRes.of(null);
    }
}
