package insty.domain.mention.controller;

import insty.domain.mention.dto.MentionUserSearchReq;
import insty.domain.mention.dto.MentionUserSearchRes;
import insty.domain.mention.service.MentionService;
import insty.global.annotation.CurrentUser;
import insty.global.response.SuccessRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Tag(name = "멘션 API")
@RestController
@RequestMapping("/api/v1/mentions")
@RequiredArgsConstructor
public class MentionController {

    private final MentionService mentionService;

    @Operation(summary = "멘션 가능한 사용자 검색", description = "멘션할 수 있는 사용자 목록을 검색한다. (본인 제외)")
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/users/search")
    public SuccessRes<List<MentionUserSearchRes>> searchMentionableUsers(
            @Valid @ModelAttribute MentionUserSearchReq searchReq,
            @CurrentUser Long userId
    ) {
        return SuccessRes.of(mentionService.searchMentionableUsers(searchReq, userId));
    }
}
