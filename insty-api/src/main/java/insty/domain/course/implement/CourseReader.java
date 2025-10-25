package insty.domain.course.implement;

import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseProgressRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseReader {

    private final CourseRepository courseRepository;
    private final CourseInstallEnvChecklistRepository courseInstallEnvChecklistRepository;
    private final CourseKeypointRepository courseKeypointRepository;
    private final CourseTagRepository courseTagRepository;
    private final CourseProgressRepository courseProgressRepository;

    public List<CourseInstallEnvChecklistInfo> getChecklistsByCourseId(Long courseId) {
        return courseInstallEnvChecklistRepository.findAllByCourseId(courseId).stream()
                .map(checklist -> new CourseInstallEnvChecklistInfo(checklist.getContent(), checklist.isSupported()))
                .toList();
    }

    public List<String> getKeypointContentsByCourseId(Long courseId) {
        return courseKeypointRepository.findAllByCourseId(courseId).stream()
                .map(CourseKeypoint::getContent)
                .toList();
    }

    public List<String> getTagNamesByCourseId(Long courseId) {
        return courseTagRepository.findAllTagsByCourseId(courseId).stream()
                .map(Tags::getTagName)
                .toList();
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));
    }

    public boolean getCourseProgressExists(Long userId, Long courseId) {
        return courseProgressRepository.existsByUserIdAndCourseId(userId,courseId);
    }


}
