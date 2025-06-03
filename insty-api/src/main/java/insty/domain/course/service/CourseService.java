package insty.domain.course.service;

import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseDetailRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseMySearchReq;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.dto.CourseSearchReq;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.implement.CourseCounter;
import insty.domain.course.implement.CourseFileWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseWriter;
import insty.domain.tag.implement.TagWriter;
import insty.model.course.Course;
import insty.model.course.CourseInstallEnvChecklist;
import insty.model.course.CourseKeypoint;
import insty.model.tag.Tags;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseWriter courseWriter;
    private final CourseReader courseReader;
    private final TagWriter tagWriter;
    private final CourseCounter courseCounter;
    private final CourseFileWriter courseFileWriter;

    public CourseDetailRes createCourse(CourseCreateReq req, MultipartFile thumbnail, MultipartFile[] practiceFile) {
        // TODO - 실습자료 저장
        Course course = courseWriter.saveCourse(req, null);
        String thumbnailUrl = courseFileWriter.saveThumbnailAndGetUrl(thumbnail, course);
        List<CourseInstallEnvChecklist> checklists = courseWriter.saveCourseInstallEnvChecklist(course,
                req.installEnvChecklist());
        List<CourseKeypoint> keypoints = courseWriter.saveCourseKeypoints(course, req.keyPoints());
        Set<Tags> tags = tagWriter.saveTags(req.tags());
        courseWriter.saveCourseTags(course, tags);

        return CourseDetailRes.from(course, checklists, keypoints, tags, thumbnailUrl);
    }

    public CourseDetailRes updateCourse(Long courseId, CourseUpdateReq req, MultipartFile thumbnail,
                                        MultipartFile[] practiceFile) {
        // TODO - 파일들이 null이 아니면 기존 파일들 삭제하고 새 썸네일/실습자료 추가
        Course course = courseWriter.updateCourse(courseId, req);
        List<CourseInstallEnvChecklist> checklists = courseWriter.updateCourseInstallEnvChecklist(course,
                req.installEnvChecklist());
        List<CourseKeypoint> keypoints = courseWriter.updateCourseKeypoints(course, req.keyPoints());
        Set<Tags> tags = tagWriter.saveTags(req.tags());
        courseWriter.updateCourseTags(course, tags);

        // TODO - 썸네일 url
        return CourseDetailRes.from(course, checklists, keypoints, tags, null);
    }

    /**
     * CourseTag만 삭제하고, Course는 isDeleted=true만 설정하여 논리적 삭제한다.
     *
     * @param courseId
     */
    public void deleteCourse(Long courseId) {
        // TODO - 게시자와 동일한 유저인지 검증
        Course course = courseReader.getCourseById(courseId);
        courseWriter.deleteAllCourseTags(course.getId());
        courseWriter.deleteCourse(course);
    }

    public CourseDetailRes detailCourse(Long courseId) {
        Course course = courseCounter.increaseViewCountAndGetCourse(courseId);
        List<CourseInstallEnvChecklistInfo> checklists = courseReader.getChecklistsByCourseId(course.getId());
        List<String> keypoints = courseReader.getKeypointContentsByCourseId(course.getId());
        List<String> tagNames = courseReader.getTagNamesByCourseId(course.getId());

        // TODO - 썸네일 url
        return CourseDetailRes.from(course, checklists, keypoints, tagNames, null);
    }

    public SearchRes<CourseSearchInfo> searchCourse(CourseSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseSearchFilter filter = req.toSearchFilter();

        List<CourseSearchInfo> searchInfo = courseReader.searchCourse(paginationReq, filter);
        PaginationRes paginationRes = courseReader.countSearchCourse(paginationReq, filter);

        return SearchRes.from(paginationRes, searchInfo);
    }

    public SearchRes<CourseMySearchInfo> searchMyCourse(Long userId, CourseMySearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        // TODO - 유저 아이디 필터

        List<CourseMySearchInfo> searchInfo = courseReader.searchMyCourse(paginationReq, userId);
        PaginationRes paginationRes = courseReader.countSearchMyCourse(paginationReq, userId);

        return SearchRes.from(paginationRes, searchInfo);
    }
}
