package insty.domain.course.controller;

import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.dto.CoursePostRes;
import insty.domain.course.service.CourseService;
import insty.global.annotation.CustomExceptionDescription;
import insty.global.response.SuccessRes;
import insty.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
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
    @CustomExceptionDescription(SwaggerResponseDescription.COURSE_POST)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessRes<CoursePostRes> courseCreate(
            @RequestPart("coursePostReq") @Validated CoursePostReq req,
            @Parameter(description = "썸네일", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "실습자료(최대 2개)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart(value = "practiceFile", required = false) @Size(max = 2) MultipartFile[] practiceFile
    ) {
        return SuccessRes.of(courseService.createCourse(req, thumbnail, practiceFile));
    }
}
