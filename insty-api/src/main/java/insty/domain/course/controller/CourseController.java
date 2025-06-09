package insty.domain.course.controller;

import insty.domain.common.SearchRes;
import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseDetailRes;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseMySearchReq;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.dto.CourseSearchReq;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.service.CourseService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
            @RequestPart("coursePostReq") @Validated CourseCreateReq req,
            @Parameter(description = "썸네일", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "실습자료(최대 2개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "practiceFile", required = false) @Size(max = 2) List<MultipartFile> practiceFile
    ) {
        return SuccessRes.of(courseService.createCourse(req, thumbnail, practiceFile));
    }

    @Operation(summary = "강의 수정", description = "강의를 수정한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_UPDATE)
    @PreAuthorize("hasRole('CREATOR')")
    @PutMapping(path = "/{courseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CourseDetailRes> courseUpdate(
            @PathVariable("courseId") Long courseId,
            @RequestPart("courseUpdateReq") @Validated CourseUpdateReq req,
            @Parameter(description = "썸네일", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "실습자료(최대 2개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "practiceFile", required = false) @Size(max = 2) List<MultipartFile> practiceFile
    ) {
        return SuccessRes.of(courseService.updateCourse(courseId, req, thumbnail, practiceFile));
    }

    @Operation(summary = "강의 삭제", description = "강의를 삭제한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_DELETE)
    @PreAuthorize("hasRole('CREATOR')")
    @DeleteMapping("/{courseId}")
    public SuccessRes<?> courseDelete(
            @PathVariable("courseId") Long courseId
    ) {
        courseService.deleteCourse(courseId);
        return SuccessRes.of(null);
    }

    @Operation(summary = "강의 상세조회", description = "강의를 상세조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_DETAIL)
    @PreAuthorize("hasRole('LEARNER') or hasRole('CREATOR')")
    @GetMapping("/{courseId}")
    public SuccessRes<CourseDetailRes> courseDetail(
            @PathVariable("courseId") Long courseId
    ) {
        return SuccessRes.of(courseService.detailCourse(courseId));
    }

    @Operation(summary = "강의 목록조회", description = "강의 목록을 조회한다.")
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_SEARCH)
    @PreAuthorize("hasRole('LEARNER')")
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
            @ModelAttribute @Validated CourseMySearchReq req
    ) {
        Long userId = 1L; // TODO - 인증 정보로부터 추출
        return SuccessRes.of(courseService.searchMyCourse(userId, req));
    }
}
