package insty.domain.course.service;

import insty.domain.common.FileInfo;
import insty.domain.common.SearchRes;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.community.implement.CommunityQuestionReader;
import insty.domain.course.dto.CourseCreateReq;
import insty.domain.course.dto.CourseDetailRes;
import insty.domain.course.dto.CourseInstallEnvChecklistInfo;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseMySearchReq;
import insty.domain.course.dto.CourseProgressRes;
import insty.domain.course.dto.CourseProgressSearchInfo;
import insty.domain.course.dto.CourseProgressSearchReq;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.dto.CourseSearchReq;
import insty.domain.course.dto.CourseUpdateReq;
import insty.domain.course.implement.CourseComplexReader;
import insty.domain.course.implement.CourseCounter;
import insty.domain.course.implement.CourseFileReader;
import insty.domain.course.implement.CourseFileWriter;
import insty.domain.course.implement.CourseProgressValidator;
import insty.domain.course.implement.CourseProgressWriter;
import insty.domain.course.implement.CourseReader;
import insty.domain.course.implement.CourseTagWriter;
import insty.domain.course.implement.CourseValidator;
import insty.domain.course.implement.CourseVideoManager;
import insty.domain.course.implement.CourseWriter;
import insty.domain.user.implement.UserReader;
import insty.model.course.Course;
import insty.model.course.CourseProgress;
import insty.model.user.User;
import insty.model.video.VideoCourse;
import java.util.List;
import java.util.Map;
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
    private final CourseVideoManager courseVideoManager;
    private final CourseValidator courseValidator;
    private final CourseComplexReader courseComplexReader;
    private final UserReader userReader;
    private final CourseProgressWriter courseProgressWriter;
    private final CourseProgressValidator courseProgressValidator;
    private final CommunityQuestionReader communityQuestionReader;

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
        VideoCourse videoCourse = courseVideoManager.updateAndGetLinkedVideo(course, req.updateVideoUuid());
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
     * 관련 파일(썸네일, 실습 파일, 영상)은 모두 삭제한다.
     *
     * @param courseId
     */
    public void deleteCourse(Long userId, Long courseId) {
        courseValidator.validateCourseOwner(courseId, userId);
        Course course = courseReader.getCourseById(courseId);
        courseTagWriter.deleteAllCourseTags(course.getId());
        courseFileWriter.deleteAllFiles(course);
        courseVideoManager.deleteCourseVideo(course.getId());
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

    @Transactional(readOnly = true)
    public SearchRes<CourseProgressSearchInfo> searchCourseProgresses(Long userId, CourseProgressSearchReq req) {
        PaginationReq paginationReq = req.toPaginationReq();

        List<CourseProgressSearchInfo> searchInfo = courseComplexReader.searchCourseProgresses(paginationReq,userId);
        searchInfo = courseComplexReader.setBasicThumbnailUrlForCourseProgress(searchInfo);

        List<Long> courseIds = searchInfo.stream()
                .map(CourseProgressSearchInfo::courseId)
                .toList();
        Map<Long, Long> countByCourseIds = communityQuestionReader.getCountByCourseIds(courseIds);

        List<CourseProgressSearchInfo> finalResult = searchInfo.stream()
                .map(dto -> CourseProgressSearchInfo.withCommentCount(dto, countByCourseIds.get(dto.courseId())))
                .toList();
        PaginationRes paginationRes = courseComplexReader.countCourseProgresses(paginationReq,userId);

        return SearchRes.from(paginationRes, finalResult);
    }

    public CourseProgressRes createCourseProgressAsCompleted(Long userId, Long courseId) {
        User user = userReader.getUser(userId);
        Course course = courseReader.getCourseById(courseId);
        courseProgressValidator.validateCourseProgressNotExists(userId, courseId);

        CourseProgress courseProgress = courseProgressWriter.saveCourseProgress(user, course);
        return CourseProgressRes.from(courseProgress);
    }
}
