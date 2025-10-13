package insty.domain.course.implement;

import insty.domain.common.ViewCountPolicy;
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

    /**
     * 동시성을 제어하기 위해 비관적 락, 수정 쿼리, 낙관적 락, Redis 등 여러가지 방식이 있다.<br> 가장 간단하고 실행 시간도 합리적인 수정 쿼리 방식을 사용한다.
     *
     * @param courseId
     * @return
     */
    public Course increaseViewCountAndGetCourse(Long courseId, ViewCountPolicy viewCountPolicy) {
        if(ViewCountPolicy.SKIP != viewCountPolicy) {
            courseRepository.incrementViewCount(courseId);
        }
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));
    }
}
