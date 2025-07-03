package insty.domain.course.implement;

import insty.domain.course.repository.CourseRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import java.util.Set;
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

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png");

    public void validateCourseOwner(Long courseId, Long userId) {
        log.info("#@!: {}, {}", courseId, userId);
        if (!courseRepository.existsByIdAndUserId(courseId, userId)) {
            throw new CustomException(CourseErrorCode.COURSE_CANT_CHANGE);
        }
    }

    public void validateCourseThumbnailExtension(MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty()) {
            return;
        }
        String contentType = thumbnail.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new CustomException(CourseErrorCode.COURSE_THUMBNAIL_INVALID_EXTENSION);
        }
    }
}
