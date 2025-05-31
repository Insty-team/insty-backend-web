package insty.domain.course.implement;

import insty.domain.course.repository.CourseRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseCounter {

    private final CourseRepository courseRepository;

    public Course increaseViewCountAndGetCourse(Long courseId) {
        courseRepository.incrementViewCount(courseId);
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));
    }
}
