package insty.domain.course.repository;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseProgressSearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CourseQueryRepository {

    List<CourseSearchInfo> searchCourses(PaginationReq paginationReq, CourseSearchFilter filter);

    PaginationRes countSearchCourses(PaginationReq paginationReq, CourseSearchFilter filter);

    Map<Long, List<String>> getCourseTags(List<Long> courseIds);

    List<CourseMySearchInfo> searchMyCourses(PaginationReq paginationReq, Long userId, Boolean isShow);

    PaginationRes countSearchMyCourses(PaginationReq paginationReq, Long userId, Boolean isShow);

    Map<Long, UUID> getCourseVideoUuids(List<Long> courseIds);

    List<CourseProgressSearchInfo> searchCourseProgresses(PaginationReq pagination, Long userId);

    PaginationRes countSearchCourseProgresses(PaginationReq paginationReq, Long userId);
}
