package insty.domain.course.implement;

import insty.constants.VideoConstants;
import insty.domain.course.repository.CourseRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CourseValidator {

    private final CourseRepository courseRepository;

    public void validateCourseOwner(Long courseId, Long userId) {
        if (!courseRepository.existsByIdAndUserId(courseId, userId)) {
            throw new CustomException(CourseErrorCode.COURSE_CANT_CHANGE);
        }
    }

    public void validateCourseThumbnailExtension(MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return;
        }
        String contentType = thumbnail.getContentType();
        if (contentType == null || !VideoConstants.ALLOWED_THUMBNAIL_TYPES.contains(contentType)) {
            throw new CustomException(CourseErrorCode.COURSE_THUMBNAIL_INVALID_EXTENSION);
        }
    }
}
