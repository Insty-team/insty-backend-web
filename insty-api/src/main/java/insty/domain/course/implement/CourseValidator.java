package insty.domain.course.implement;

import insty.domain.course.repository.CourseRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseValidator {

    private final CourseRepository courseRepository;

    public void validateCourseOwner(Long courseId, Long userId) {
        if (!courseRepository.existsByIdAndUserId(courseId, userId)) {
            throw new CustomException(CourseErrorCode.COURSE_CANT_DELETE);
        }
    }
}
