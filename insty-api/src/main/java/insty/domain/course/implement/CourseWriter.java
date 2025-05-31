package insty.domain.course.implement;

import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CoursePostReq;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.course.CourseTag;
import insty.model.tag.Tags;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseWriter {

    private final CourseRepository courseRepository;
    private final CourseInstallEnvChecklistRepository courseInstallEnvChecklistRepository;
    private final CourseKeypointRepository courseKeypointRepository;
    private final CourseTagRepository courseTagRepository;

    public Course saveCourse(CoursePostReq req, Long thumbnailId) {
        Course course = Course.create(req.title(), req.description(), req.price(), req.targetAudience(), thumbnailId,
                req.isShow());
        return courseRepository.save(course);
    }

    public List<CourseInstallEnvChecklist> saveCourseInstallEnvChecklist(Course course,
                                                                         List<CourseInstallEnvChecklistInfo> checklistInfos) {
        return checklistInfos.stream()
                .map(checklistInfo -> {
                    CourseInstallEnvChecklist checklist = CourseInstallEnvChecklist.create(course,
                            checklistInfo.content(), checklistInfo.isSupported());
                    return courseInstallEnvChecklistRepository.save(checklist);
                })
                .toList();
    }

    public List<CourseKeypoint> saveCourseKeypoints(Course course, List<String> keypointContents) {
        return keypointContents.stream()
                .map(keypoint -> {
                    CourseKeypoint courseKeypoint = CourseKeypoint.create(course, keypoint);
                    return courseKeypointRepository.save(courseKeypoint);
                })
                .toList();
    }

    public void saveCourseTags(Course course, List<Tags> tags) {
        for (Tags tag : tags) {
            CourseTag courseTag = CourseTag.create(course, tag);
            courseTagRepository.save(courseTag);
        }
    }
}
