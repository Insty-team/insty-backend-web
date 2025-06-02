package insty.domain.course.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.repository.CourseInstallEnvChecklistRepository;
import insty.domain.course.repository.CourseKeypointRepository;
import insty.domain.course.repository.CourseQueryRepository;
import insty.domain.course.repository.CourseRepository;
import insty.domain.course.repository.CourseTagRepository;
import insty.error.CourseErrorCode;
import insty.exception.CustomException;
import insty.model.course.Course;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Map;
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
    private final CourseQueryRepository courseQueryRepository;

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

    public List<CourseSearchInfo> searchCourse(PaginationReq paginationReq, CourseSearchFilter filter) {
        // TODO - 썸네일 url 추가
        List<CourseSearchInfo> courses = courseQueryRepository.searchCourses(paginationReq, filter);

        List<Long> courseIds = courses.stream()
                .map(CourseSearchInfo::courseId)
                .toList();
        Map<Long, List<String>> courseTags = courseQueryRepository.getCourseTags(courseIds);

        return courses.stream()
                .map(dto -> CourseSearchInfo.withTags(dto, courseTags.get(dto.courseId())))
                .toList();
    }

    public PaginationRes countSearchCourse(PaginationReq paginationReq, CourseSearchFilter filter) {
        return courseQueryRepository.countSearchCourses(paginationReq, filter);
    }

    public List<CourseMySearchInfo> searchMyCourse(PaginationReq paginationReq, Long userId) {
        // TODO - 썸네일 url, 댓글 개수 추가
        List<CourseMySearchInfo> courses = courseQueryRepository.searchMyCourses(paginationReq, userId);

        List<Long> courseIds = courses.stream()
                .map(CourseMySearchInfo::courseId)
                .toList();
        Map<Long, List<String>> courseTags = courseQueryRepository.getCourseTags(courseIds);

        return courses.stream()
                .map(dto -> CourseMySearchInfo.withTags(dto, courseTags.get(dto.courseId())))
                .toList();
    }

    public PaginationRes countSearchMyCourse(PaginationReq paginationReq, Long userId) {
        return courseQueryRepository.countSearchMyCourses(paginationReq, userId);
    }
}
