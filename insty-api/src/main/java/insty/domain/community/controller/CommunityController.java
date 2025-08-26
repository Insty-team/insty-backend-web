package insty.domain.community.controller;

import insty.domain.common.SearchRes;
import insty.domain.community.dto.AcceptAnswerResultRes;
import insty.domain.community.dto.CommunityAnswerCreateReq;
import insty.domain.community.dto.CommunityAnswerRes;
import insty.domain.community.dto.CommunityAnswerUpdateReq;
import insty.domain.community.dto.CommunityQuestionCreateReq;
import insty.domain.community.dto.CommunityQuestionDetailsRes;
import insty.domain.community.dto.CommunityQuestionMyRes;
import insty.domain.community.dto.CommunityQuestionRes;
import insty.domain.community.dto.CommunityQuestionSearchReq;
import insty.domain.community.dto.CommunityQuestionUpdateReq;
import insty.domain.community.service.CommunityAnswerService;
import insty.domain.community.service.CommunityQuestionService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "커뮤니티 API")
@RestController
@RequestMapping("/api/v1/community/")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityQuestionService communityQuestionService;
    private final CommunityAnswerService communityAnswerService;

    /// ============================== 질문 API  ======================================

    @Operation(summary = "커뮤니티 질문 검색", description = "질문 목록을 조회한다. (제목/내용 키워드 검색)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions")
    public SuccessRes<SearchRes<CommunityQuestionRes>> searchQuestions(
            @ModelAttribute @Validated CommunityQuestionSearchReq req
    ) {
        return SuccessRes.of(communityQuestionService.searchQuestions(req));
    }

    @Operation(summary = "강좌별 질문 검색", description = "특정 강좌의 질문 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_COURSE_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/course/{courseId}")
    public SuccessRes<SearchRes<CommunityQuestionRes>> searchQuestionsByCourse(
            @PathVariable @NotNull Long courseId,
            @ModelAttribute @Validated CommunityQuestionSearchReq req
    ) {
        return SuccessRes.of(communityQuestionService.searchQuestionsByCourseId(req, courseId));
    }

    @Operation(summary = "유저 질문 검색", description = "러너 자신이 작성한 질문 목록을 조회한다. (인증 사용자 기준)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_MY_SEARCH)
    @PreAuthorize("hasRole('LEARNER')")
    @GetMapping("/questions/my")
    public SuccessRes<SearchRes<CommunityQuestionMyRes>> searchQuestionsByUser(
            @CurrentUser Long userId,
            @ModelAttribute @Validated CommunityQuestionSearchReq req
    ) {
        return SuccessRes.of(communityQuestionService.searchQuestionsByUserId(req, userId));
    }

    @Operation(summary = "질문 상세 조회", description = "질문의 상세 정보를 조회한다. (답변 최신순)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DETAIL)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/{questionId}")
    public SuccessRes<CommunityQuestionDetailsRes> getQuestionDetails(
            @PathVariable @NotNull Long questionId,
            @CurrentUser Long userId
    ) {
        CommunityQuestionDetailsRes response = communityQuestionService.getQuestionDetails(questionId, userId);
        return SuccessRes.of(response);
    }

    @Operation(summary = "질문 작성", description = "질문을 생성한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_CREATE)
    @PreAuthorize("hasRole('LEARNER')")
    @PostMapping(value = "/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionDetailsRes> createQuestion(
            @CurrentUser Long userId,
            @RequestPart("communityQuestionReq") @Validated CommunityQuestionCreateReq communityQuestionCreateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 최대 2개)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityQuestionService.saveQuestion(userId, communityQuestionCreateReq, attachments));
    }

    @Operation(summary = "질문 수정", description = "질문을 수정한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_UPDATE)
    @PreAuthorize("hasRole('LEARNER')")
    @PatchMapping(value = "/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityQuestionDetailsRes> updateQuestion(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CommunityQuestionUpdateReq communityQuestionUpdateReq,
            @Parameter(description = "질문 첨부파일 (이미지, 최대 2개)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 2) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityQuestionService.updateQuestion(userId, questionId, communityQuestionUpdateReq, attachments));
    }

    @Operation(summary = "질문 삭제", description = "질문을 삭제한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_QUESTION_DELETE)
    @PreAuthorize("hasRole('LEARNER')")
    @DeleteMapping("/questions/{questionId}")
    public SuccessRes<?> deleteQuestion(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId
    ) {
        communityQuestionService.deleteQuestion(userId, questionId);
        return SuccessRes.of(null);
    }

    /// ============================== 답변 API  ======================================

    @Operation(summary = "답변 조회", description = "질문의 답변 목록을 조회한다. (최신순)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/questions/{questionId}/answer")
    public SuccessRes<List<CommunityAnswerRes>> retrieveAllAnswers(@PathVariable @NotNull Long questionId) {
        return SuccessRes.of(communityAnswerService.getAllAnswersByQuestionId(questionId));
    }

    @Operation(summary = "답변 작성", description = "질문에 답변을 생성한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_CREATE)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PostMapping(value = "/questions/{questionId}/answer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityAnswerRes> createAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @RequestPart CommunityAnswerCreateReq communityAnswerCreateReq,
            @Parameter(description = "답변 첨부파일 (이미지, 최대 1개)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityAnswerService.saveAnswer(userId, questionId, communityAnswerCreateReq, attachments));
    }

    @Operation(summary = "답변 수정", description = "답변을 수정한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_UPDATE)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @PatchMapping(value = "/answer/{answerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CommunityAnswerRes> updateAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long answerId,
            @RequestPart CommunityAnswerUpdateReq communityAnswerUpdateReq,
            @Parameter(description = "답변 첨부파일 (이미지, 최대 1개)", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart(value = "attachments", required = false) @Size(max = 1) List<MultipartFile> attachments
    ) {
        return SuccessRes.of(communityAnswerService.updateAnswer(userId, answerId, communityAnswerUpdateReq, attachments));
    }


    /// ============================== 답변 채택 API  ======================================

    @Operation(summary = "답변 채택", description = "질문자가 답변을 채택/해제한다. (토글)")
    @CustomExceptionDescription(SwaggerResponseDescription.COMMUNITY_ANSWER_ACCEPT)
    @PreAuthorize("hasRole('LEARNER')")
    @PostMapping("/questions/{questionId}/answer/{answerId}/accept")
    public SuccessRes<AcceptAnswerResultRes> acceptAnswer(
            @CurrentUser Long userId,
            @PathVariable @NotNull Long questionId,
            @PathVariable @NotNull Long answerId
    ) {
        return SuccessRes.of(communityAnswerService.acceptAnswer(userId, questionId, answerId));
    }


}
