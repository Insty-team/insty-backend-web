package insty.domain.course.implement;

import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.course.CourseTag;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    public Course saveCourse(CourseCreateReq req) {
        Course course = Course.create(req.title(), req.description(), req.price(), req.targetAudience(), req.isShow());
        return courseRepository.save(course);
    }

    public List<CourseInstallEnvChecklist> saveCourseInstallEnvChecklist(Course course,
                                                                         List<CourseInstallEnvChecklistInfo> checklistInfos) {
        List<CourseInstallEnvChecklist> checklists = checklistInfos.stream()
                .map(checklistInfo -> CourseInstallEnvChecklist.create(course,
                        checklistInfo.content(), checklistInfo.isSupported()))
                .toList();
        return courseInstallEnvChecklistRepository.saveAll(checklists);
    }

    public List<CourseKeypoint> saveCourseKeypoints(Course course, List<String> keypointContents) {
        List<CourseKeypoint> keypoints = keypointContents.stream()
                .map(keypoint -> CourseKeypoint.create(course, keypoint))
                .toList();
        return courseKeypointRepository.saveAll(keypoints);
    }

    public void saveCourseTags(Course course, Set<Tags> tags) {
        List<CourseTag> list = tags.stream()
                .map(tag -> CourseTag.create(course, tag))
                .toList();
        courseTagRepository.saveAll(list);
    }

    public Course updateCourse(Long courseId, CourseUpdateReq req) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));
        course.update(req.title(), req.description(), req.price(), req.targetAudience());
        return courseRepository.save(course);
    }

    public List<CourseInstallEnvChecklist> updateCourseInstallEnvChecklist(Course course,
                                                                           List<CourseInstallEnvChecklistInfo> checklistInfos) {
        courseInstallEnvChecklistRepository.deleteAllByCourseId(course.getId());
        return saveCourseInstallEnvChecklist(course, checklistInfos);
    }

    public List<CourseKeypoint> updateCourseKeypoints(Course course, List<String> keypointContents) {
        courseKeypointRepository.deleteAllByCourseId(course.getId());
        return saveCourseKeypoints(course, keypointContents);
    }

    public void updateCourseTags(Course course, Set<Tags> tags) {
        List<Long> tagIds = tags.stream()
                .map(Tags::getId)
                .toList();
        Set<Long> existingTagIds = courseTagRepository.findAllExistsTagIdsByCourseIdAndTagIdIn(
                course.getId(), tagIds);
        Set<Tags> saveTargetTags = tags.stream()
                .filter(tag -> !existingTagIds.contains(tag.getId()))
                .collect(Collectors.toSet());
        saveCourseTags(course, saveTargetTags);
    }

    public void deleteAllCourseTags(Long courseId) {
        courseTagRepository.deleteAllByCourseId(courseId);
    }

    public void deleteCourse(Course course) {
        course.deleteLogically();
        courseRepository.save(course);
    }
}
