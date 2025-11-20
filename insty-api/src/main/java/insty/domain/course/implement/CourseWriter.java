package insty.domain.course.implement;

import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.user.User;
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

    public Course saveCourse(User user, CourseCreateReq req) {
        Course course = Course.create(user, req.title(), req.description(), req.price(), req.targetAudience(),
                req.isShow());
        return courseRepository.save(course);
    }

    public Course patchCourseIsShow(Long courseId,boolean isShow){
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));
        course.changeIsShow(isShow);
        return course;
    }

    public List<CourseInstallEnvChecklistInfo> saveCourseInstallEnvChecklist(Course course,
                                                                             List<CourseInstallEnvChecklistInfo> checklistInfos) {
        List<CourseInstallEnvChecklist> checklists = checklistInfos.stream()
                .map(checklistInfo -> CourseInstallEnvChecklist.create(course,
                        checklistInfo.content(), checklistInfo.isSupported()))
                .toList();
        return courseInstallEnvChecklistRepository.saveAll(checklists).stream()
                .map(CourseInstallEnvChecklistInfo::from)
                .toList();
    }

    public List<String> saveCourseKeypoints(Course course, List<String> keypointContents) {
        List<CourseKeypoint> keypoints = keypointContents.stream()
                .map(keypoint -> CourseKeypoint.create(course, keypoint))
                .toList();
        return courseKeypointRepository.saveAll(keypoints).stream()
                .map(CourseKeypoint::getContent)
                .toList();
    }

    public Course updateCourse(Long courseId, CourseUpdateReq req) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));
        course.update(req.title(), req.description(), req.price(), req.targetAudience());
        return courseRepository.save(course);
    }

    public List<CourseInstallEnvChecklistInfo> updateCourseInstallEnvChecklist(Course course,
                                                                               List<CourseInstallEnvChecklistInfo> checklistInfos) {
        courseInstallEnvChecklistRepository.deleteAllByCourseId(course.getId());
        return saveCourseInstallEnvChecklist(course, checklistInfos);
    }

    public List<String> updateCourseKeypoints(Course course, List<String> keypointContents) {
        courseKeypointRepository.deleteAllByCourseId(course.getId());
        return saveCourseKeypoints(course, keypointContents);
    }

    /**
     * cascade 없이 연관된 데이터를 먼저 삭제하고 강의를 마지막에 삭제할 것
     */
    public void deleteCourse(Course course) {
        courseInstallEnvChecklistRepository.deleteAllByCourseId(course.getId());
        courseKeypointRepository.deleteAllByCourseId(course.getId());
        courseRepository.delete(course);
    }
}
