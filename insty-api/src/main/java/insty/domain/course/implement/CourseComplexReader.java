package insty.domain.course.implement;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseProgressSearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.community.repository.CommunityPostCountProjection;
import insty.domain.community.repository.CommunityPostRepository;
import insty.domain.course.repository.CourseQueryRepository;
import insty.domain.course.repository.CourseRepository;
import insty.global.property.AppProperties;
import insty.model.course.Course;
import insty.util.VideoUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CourseComplexReader {

    private final CourseRepository courseRepository;
    private final CourseQueryRepository courseQueryRepository;
    private final CommunityPostRepository communityPostRepository;

    private final AppProperties appProperties;

    public List<CourseSearchInfo> searchCourse(PaginationReq paginationReq, CourseSearchFilter filter) {
        List<CourseSearchInfo> courses = courseQueryRepository.searchCourses(paginationReq, filter);

        List<Long> courseIds = courses.stream()
                .map(CourseSearchInfo::courseId)
                .toList();
        Map<Long, List<String>> courseTags = courseQueryRepository.getCourseTags(courseIds);
        Map<Long, String> thumbnailUrls = getCourseThumbnailUrlMap(courseIds);

        return courses.stream()
                .map(dto -> CourseSearchInfo.assembly(
                        dto,
                        courseTags.get(dto.courseId()),
                        thumbnailUrls.get(dto.courseId())))
                .toList();
    }

    public PaginationRes countSearchCourse(PaginationReq paginationReq, CourseSearchFilter filter) {
        return courseQueryRepository.countSearchCourses(paginationReq, filter);
    }

    public List<CourseMySearchInfo> searchMyCourse(PaginationReq paginationReq, Long userId, Boolean isShow) {
        List<CourseMySearchInfo> courses = courseQueryRepository.searchMyCourses(paginationReq, userId, isShow);

        List<Long> courseIds = courses.stream()
                .map(CourseMySearchInfo::courseId)
                .toList();
        Map<Long, List<String>> courseTags = courseQueryRepository.getCourseTags(courseIds);
        Map<Long, String> thumbnailUrls = getCourseThumbnailUrlMap(courseIds);
        Map<Long, Long> communityPostCounts = courseIds.isEmpty()
                ? Map.of()
                : communityPostRepository.countByCourseIds(courseIds).stream()
                        .collect(Collectors.toMap(
                                CommunityPostCountProjection::getCourseId,
                                CommunityPostCountProjection::getCount
                        ));

        return courses.stream()
                .map(dto -> CourseMySearchInfo.assembly(
                        dto,
                        courseTags.get(dto.courseId()),
                        thumbnailUrls.get(dto.courseId())
                ))
                .map(dto -> CourseMySearchInfo.withCommunityPostCount(
                        dto,
                        communityPostCounts.getOrDefault(dto.courseId(), 0L)
                ))
                .toList();
    }

    public PaginationRes countSearchMyCourse(PaginationReq paginationReq, Long userId, Boolean isShow) {
        return courseQueryRepository.countSearchMyCourses(paginationReq, userId, isShow);
    }

    /**
     * Learner가 (수강했던, 수강중인, 수강이 완료된) 강좌를 필터, 검색 조건, 정렬을 기준으로 검색
     */
    public List<CourseProgressSearchInfo> searchCourseProgresses(PaginationReq paginationReq, Long userId) {
        List<CourseProgressSearchInfo> courseProgresses = courseQueryRepository.searchCourseProgresses(paginationReq, userId);
        List<Long> courseIds = courseProgresses.stream()
                .map(CourseProgressSearchInfo::courseId)
                .toList();
        Map<Long, String> thumbnailUrls = getCourseThumbnailUrlMap(courseIds);

        return courseProgresses.stream()
                .map(dto -> CourseProgressSearchInfo.assembleWithNoUrl(
                        dto,
                        thumbnailUrls.get(dto.courseId())
                ))
                .toList();
    }

    /**
     * Learner가 강좌 총 검색 개수
     */
    public PaginationRes countCourseProgresses(PaginationReq paginationReq, Long userId) {
        return courseQueryRepository.countSearchCourseProgresses(paginationReq,userId);
    }

    public List<CourseProgressSearchInfo> setBasicThumbnailUrlForCourseProgress(List<CourseProgressSearchInfo> searchInfo) {
        return setBasicThumbnailUrl(
                searchInfo,
                CourseProgressSearchInfo::courseId,
                CourseProgressSearchInfo::thumbnailUrl,
                CourseProgressSearchInfo::setThumbnailUrl
        );
    }

    private Map<Long, String> getCourseThumbnailUrlMap(List<Long> courseIds) {
        return courseRepository.findWithThumbnailByCourseIdIn(courseIds).stream()
                .collect(Collectors.toMap(
                        Course::getId,
                        course -> course.getThumbnail().getUrl(appProperties.getDomain())
                ));
    }

    public List<CourseSearchInfo> setBasicThumbnailUrlForSearch(List<CourseSearchInfo> searchInfo) {
        return setBasicThumbnailUrl(
                searchInfo,
                CourseSearchInfo::courseId,
                CourseSearchInfo::thumbnailUrl,
                CourseSearchInfo::setThumbnailUrl
        );
    }

    public List<CourseMySearchInfo> setBasicThumbnailUrlForMy(List<CourseMySearchInfo> searchInfo) {
        return setBasicThumbnailUrl(
                searchInfo,
                CourseMySearchInfo::courseId,
                CourseMySearchInfo::thumbnailUrl,
                CourseMySearchInfo::setThumbnailUrl
        );
    }

    /**
     * CourseSearchInfo, CourseMySearchInfo 기본 썸네일 url 삽입 로직 공통화를 위한 메서드
     *
     * @param searchInfo      dto 리스트
     * @param getCourseId     courseId 추출 함수
     * @param getThumbnailUrl thumbnailUrl 추출 함수
     * @param setThumbnailUrl 썸네일 세팅 함수
     * @param <T>             CourseSearchInfo/CourseMySearchInfo
     * @return 기본 썸네일이 들어간 새 리스트
     */
    private <T> List<T> setBasicThumbnailUrl(
            List<T> searchInfo,
            Function<T, Long> getCourseId,
            Function<T, String> getThumbnailUrl,
            BiFunction<T, String, T> setThumbnailUrl
    ) {
        List<Long> courseIds = searchInfo.stream()
                .filter(info -> getThumbnailUrl.apply(info) == null)
                .map(getCourseId)
                .toList();

        Map<Long, UUID> courseVideoUuids = courseQueryRepository.getCourseVideoUuids(courseIds);

        return searchInfo.stream()
                .map(info -> {
                    if (getThumbnailUrl.apply(info) != null || !courseVideoUuids.containsKey(getCourseId.apply(info))) {
                        return info;
                    }
                    String baseThumbnailUrl = VideoUtils.getVideoBasicThumbnailUrl(
                            appProperties.getDomain(),
                            courseVideoUuids.get(getCourseId.apply(info))
                    );
                    return setThumbnailUrl.apply(info, baseThumbnailUrl);
                })
                .toList();
    }
}
