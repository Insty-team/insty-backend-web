package insty.domain.course.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseDetailRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseMySearchReq;
import insty.domain.course.dto.CourseRequestReq;
import insty.domain.course.dto.CourseRequestRes;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.dto.CourseSearchReq;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.implement.CourseComplexReader;
import insty.domain.course.implement.CourseCounter;
import insty.domain.course.implement.CourseFileReader;
import insty.domain.course.implement.CourseFileWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseRequestReader;
import insty.domain.course.implement.CourseRequestWriter;
import insty.domain.course.implement.CourseTagWriter;
import insty.domain.course.implement.CourseValidator;
import insty.domain.course.implement.CourseVideoManager;
import insty.domain.course.implement.CourseWriter;
import insty.domain.user.implement.UserReader;
import insty.model.course.Course;
import insty.model.course.CourseRequest;
import insty.model.user.User;
import insty.model.video.VideoCourse;
import java.util.List;
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
    private final CourseCounter courseCounter;
    private final CourseFileWriter courseFileWriter;
    private final CourseFileReader courseFileReader;
    private final CourseTagWriter courseTagWriter;
    private final CourseRequestWriter courseRequestWriter;
    private final CourseRequestReader courseRequestReader;
    private final CourseVideoManager courseVideoManager;
    private final CourseValidator courseValidator;
    private final CourseComplexReader courseComplexReader;
    private final UserReader userReader;

    public CourseDetailRes createCourse(Long userId, CourseCreateReq req, MultipartFile thumbnail,
                                        List<MultipartFile> practiceFile) {
        courseValidator.validateCourseThumbnailExtension(thumbnail);
        User user = userReader.getUser(userId);
        Course course = courseWriter.saveCourse(user, req);
        VideoCourse videoCourse = courseVideoManager.attachmentCourse(course, req.videoUuid());
        courseFileWriter.saveThumbnail(thumbnail, course);
        String thumbnailUrl = courseFileReader.getThumbnailUrl(course, videoCourse.getVideoUuid());
        List<FileInfo> practiceFileInfos = courseFileWriter.savePracticeFilesAndGetInfo(practiceFile, course);
        List<CourseInstallEnvChecklistInfo> checklists = courseWriter.saveCourseInstallEnvChecklist(course,
                req.installEnvChecklist());
        List<String> keypoints = courseWriter.saveCourseKeypoints(course, req.keyPoints());
        List<String> tags = courseTagWriter.saveCourseTagsAndGetTagNames(course, req.tags());

        return CourseDetailRes.from(course, user, checklists, keypoints, tags, thumbnailUrl, practiceFileInfos,
                videoCourse);
    }

    public CourseDetailRes updateCourse(Long userId, Long courseId, CourseUpdateReq req, MultipartFile thumbnail,
                                        List<MultipartFile> practiceFile) {
        courseValidator.validateCourseOwner(courseId, userId);
        courseValidator.validateCourseThumbnailExtension(thumbnail);
        Course course = courseWriter.updateCourse(courseId, req);
        User user = course.getUser();
        VideoCourse videoCourse = courseVideoManager.updateVideo(course, req.updateVideoUuid());
        courseFileWriter.updateThumbnail(thumbnail, course);
        String thumbnailUrl = courseFileReader.getThumbnailUrl(course, videoCourse.getVideoUuid());
        List<FileInfo> fileInfos = courseFileWriter.updatePracticeFilesAndGetInfo(practiceFile,
                req.deletePracticeFileId(), course);
        List<CourseInstallEnvChecklistInfo> checklists = courseWriter.updateCourseInstallEnvChecklist(course,
                req.installEnvChecklist());
        List<String> keypoints = courseWriter.updateCourseKeypoints(course, req.keyPoints());
        List<String> tags = courseTagWriter.updateCourseTags(course, req.tags());

        return CourseDetailRes.from(course, user, checklists, keypoints, tags, thumbnailUrl, fileInfos, videoCourse);
    }

    /**
     * CourseTag만 삭제하고, Course는 isDeleted=true만 설정하여 논리적 삭제한다.<br> 관련 파일은 모두 삭제한다.
     *
     * @param courseId
     */
    public void deleteCourse(Long userId, Long courseId) {
        courseValidator.validateCourseOwner(courseId, userId);
        Course course = courseReader.getCourseById(courseId);
        courseTagWriter.deleteAllCourseTags(course.getId());
        courseFileWriter.deleteAllFiles(course);
        courseWriter.deleteCourse(course);
    }

    public CourseDetailRes detailCourse(Long courseId) {
        Course course = courseCounter.increaseViewCountAndGetCourse(courseId);
        User creator = course.getUser();
        List<CourseInstallEnvChecklistInfo> checklists = courseReader.getChecklistsByCourseId(course.getId());
        List<String> keypoints = courseReader.getKeypointContentsByCourseId(course.getId());
        List<String> tagNames = courseReader.getTagNamesByCourseId(course.getId());
        VideoCourse videoCourse = courseVideoManager.getAttachCourseVideo(course.getId());
        String thumbnailUrl = courseFileReader.getThumbnailUrl(course, videoCourse.getVideoUuid());
        List<FileInfo> practiceFiles = courseFileReader.getPracticeFiles(course);

        return CourseDetailRes.from(course, creator, checklists, keypoints, tagNames, thumbnailUrl, practiceFiles,
                videoCourse);
    }

    public SearchRes<CourseSearchInfo> searchCourse(CourseSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();
        CourseSearchFilter filter = req.toSearchFilter();

        List<CourseSearchInfo> searchInfo = courseComplexReader.searchCourse(paginationReq, filter);
        searchInfo = courseComplexReader.setBasicThumbnailUrlForSearch(searchInfo);
        PaginationRes paginationRes = courseComplexReader.countSearchCourse(paginationReq, filter);

        return SearchRes.from(paginationRes, searchInfo);
    }

    public SearchRes<CourseMySearchInfo> searchMyCourse(Long userId, CourseMySearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();

        List<CourseMySearchInfo> searchInfo = courseComplexReader.searchMyCourse(paginationReq, userId);
        searchInfo = courseComplexReader.setBasicThumbnailUrlForMy(searchInfo);
        PaginationRes paginationRes = courseComplexReader.countSearchMyCourse(paginationReq, userId);

        return SearchRes.from(paginationRes, searchInfo);
    }

    /**
     * 러너가 크리에이터에게 강의 요청
     */
    public CourseRequestRes createCourseRequest(Long userId, CourseRequestReq req) {
        CourseRequest saveCourseRequest = courseRequestWriter.saveCourseRequest(userId, req);
        return CourseRequestRes.from(saveCourseRequest);
    }

    /**
     * 크리에이터에 요청된 강의 목록 조회
     */
    public List<CourseRequestRes> searchCourseRequest(Long userId) {
        List<CourseRequest> findMyCourseRequest = courseRequestReader.getListMyCourseRequest(userId);
        return CourseRequestRes.from(findMyCourseRequest);
    }
}
