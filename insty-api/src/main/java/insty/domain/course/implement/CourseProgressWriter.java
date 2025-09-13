package insty.domain.course.implement;

import insty.domain.course.repository.CourseProgressRepository;
import insty.model.course.Course;
import insty.model.course.CourseProgress;
import insty.model.course.CourseProgressStatus;
import insty.model.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseProgressWriter {
    private final CourseProgressRepository courseProgressRepository;

    public CourseProgress saveCourseProgress(User user, Course course) {
        CourseProgress courseProgress = CourseProgress.create(user, course, CourseProgressStatus.COMPLETED);
        return courseProgressRepository.save(courseProgress);
    }

}
