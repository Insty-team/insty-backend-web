package insty.domain.course.implement;


import insty.domain.course.repository.CourseProgressRepository;
import insty.error.CourseProgressErrorCode;
import insty.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CourseProgressValidator {

    private final CourseProgressRepository courseProgressRepository;

    public void validateCourseProgressNotExists(Long userId, Long courseId) {
        if(courseProgressRepository.existsByUserIdAndCourseId(userId,courseId)){
            throw new CustomException(CourseProgressErrorCode.COURSE_PROGRESS_DUPLICATE);
        }
    }

}
