package insty.domain.community.controller;

import insty.domain.community.service.CommunityService;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community/")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/questions/{question_id}")
    public SuccessRes<?> selectQuestionDetails() {

    }
}
