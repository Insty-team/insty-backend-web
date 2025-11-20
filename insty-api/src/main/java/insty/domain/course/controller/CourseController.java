package insty.domain.course.controller;

import insty.domain.common.SearchRes;
import insty.domain.common.ViewCountPolicy;
import insty.domain.course.dto.*;
import insty.domain.course.service.CourseService;
import insty.global.annotation.CurrentUser;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "강의 API")
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "강의 게시", description = "새로운 강의를 게시한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_CREATE)
    @PreAuthorize("hasRole('CREATOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseDetailRes> courseCreate(
            @CurrentUser Long userId,
            @RequestPart("coursePostReq") @Validated CourseCreateReq req,
            @Parameter(description = "썸네일", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "실습자료(최대 2개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "practiceFile", required = false) @Size(max = 2) List<MultipartFile> practiceFile
    ) {
        return SuccessRes.of(courseService.createCourse(userId, req, thumbnail, practiceFile));
    }

    @Operation(summary = "강의 수정", description = "강의를 수정한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_UPDATE)
    @PreAuthorize("hasRole('CREATOR')")
    @PutMapping(path = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseDetailRes> courseUpdate(
            @CurrentUser Long userId,
            @PathVariable("courseId") Long courseId,
            @RequestPart("courseUpdateReq") @Validated CourseUpdateReq req,
            @Parameter(description = "썸네일", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "실습자료(최대 2개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "practiceFile", required = false) @Size(max = 2) List<MultipartFile> practiceFile
    ) {
        return SuccessRes.of(courseService.updateCourse(userId, courseId, req, thumbnail, practiceFile));
    }

    @Operation(summary = "강의 삭제", description = "강의를 삭제한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_DELETE)
    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/{courseId}")
    public SuccessRes<?> courseDelete(
            @CurrentUser Long userId,
            @PathVariable("courseId") Long courseId
    ) {
        courseService.deleteCourse(userId, courseId);
        return SuccessRes.of(null);
    }

    @Operation(summary = "강의 상세조회(크리에이터용)", description = "크리에이터가 자신의 강의를 조회한다. 조회수는 증가하지 않는다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_DETAIL)
    @PreAuthorize("hasRole('CREATOR')")
    @GetMapping("/creator/{courseId}")
    public SuccessRes<CourseDetailRes> courseDetailFromCreator(
            @PathVariable("courseId") Long courseId
    ) {
        return SuccessRes.of(courseService.detailCourse(courseId, ViewCountPolicy.SKIP));
    }

    @Operation(summary = "강의 상세조회", description = "강의를 상세조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_DETAIL)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/{courseId}")
    public SuccessRes<CourseDetailRes> courseDetail(
            @PathVariable("courseId") Long courseId
    ) {
        return SuccessRes.of(courseService.detailCourse(courseId, ViewCountPolicy.INCREASE));
    }

    @Operation(summary = "강의 목록조회", description = "강의 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_SEARCH)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping
    public SuccessRes<SearchRes<CourseSearchInfo>> courseSearch(
            @ModelAttribute @Validated CourseSearchReq req
    ) {
        return SuccessRes.of(courseService.searchCourse(req));
    }

    @Operation(summary = "내가 업로드한 강의 목록조회", description = "해당 크리에이터가 업로드한 강의 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_MY_SEARCH)
    @PreAuthorize("hasRole('CREATOR')")
    @GetMapping("/my")
    public SuccessRes<SearchRes<CourseMySearchInfo>> courseMySearch(
            @CurrentUser Long userId,
            @ModelAttribute @Validated CourseMySearchReq req
    ) {
        return SuccessRes.of(courseService.searchMyCourse(userId, req));
    }

    @Operation(summary = "내가 수강중인 강의 목록조회", description = "해당 러너가 수강중인 강의 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_PROGRESS_SEARCH)
    @PreAuthorize("hasRole('LEARNER')")
    @GetMapping("/courseProgress")
    public SuccessRes<SearchRes<CourseProgressSearchInfo>> courseProgressSearch(
            @CurrentUser Long userId,
            @ModelAttribute @Validated CourseProgressSearchReq req
    ) {
        return SuccessRes.of(courseService.searchCourseProgresses(userId, req));
    }

    @Operation(summary = "강좌 수강하기", description = "러너가 수강신청을 통해 강의를 수강한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_PROGRESS_CREATE)
    @PreAuthorize("hasRole('LEARNER')")
    @PostMapping("/courseProgress/{courseId}")
    public SuccessRes<CourseProgressRes> courseProgressCreate(
            @CurrentUser Long userId,
            @PathVariable("courseId") Long courseId
    ) {
        return SuccessRes.of(courseService.createCourseProgressAsCompleted(userId,courseId));
    }


    @Operation(summary = "강좌 수강 여부 조회", description = "userId와 courseId를 기준으로 강좌 수강 여부를 단일 조회한다.")
    @PreAuthorize("hasRole('LEARNER')")
    @GetMapping("/courseProgress/{courseId}/exists")
    public SuccessRes<Boolean> searchCourseProgressExists(
            @CurrentUser Long userId,
            @PathVariable("courseId") Long courseId
    ) {
        return SuccessRes.of(courseService.searchCourseProgressExists(userId, courseId));
    }

    @Operation(summary = "강좌의 visible 상태 변경", description = "강좌의 visible상태를 변경함으로써 러너에게 보여질지 말지를 결정할 수 있다.")
    @PreAuthorize("hasRole('CREATOR')")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_VISIBLE)
    @PutMapping("/{courseId}/visibility")
    public SuccessRes<CoursePatchVisibleRes> updateCourseVisibility(
            @PathVariable("courseId") Long courseId,
            @CurrentUser Long userId,
            @RequestParam("isShow") boolean isShow
    ) {
        return SuccessRes.of(courseService.patchCourseVisible(userId,courseId, isShow));
    }
}
