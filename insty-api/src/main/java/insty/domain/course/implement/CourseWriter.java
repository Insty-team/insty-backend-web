package insty.domain.course.implement;

import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.repository.CourseRepository;
import insty.model.course.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseWriter {

    private final CourseRepository courseRepository;

    public Course saveCourse(CoursePostReq req, Long thumbnailId) {
        Course course = Course.create(req.title(), req.description(), req.price(), req.targetAudience(), thumbnailId);
        return courseRepository.save(course);
    }
}
