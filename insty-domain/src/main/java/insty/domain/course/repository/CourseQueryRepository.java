package insty.domain.course.repository;

import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.course.dto.CourseMySearchInfo;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import java.util.List;
import java.util.Map;

public interface CourseQueryRepository {

    List<CourseSearchInfo> searchCourses(PaginationReq paginationReq, CourseSearchFilter filter);

    PaginationRes countSearchCourses(PaginationReq paginationReq, CourseSearchFilter filter);

    Map<Long, List<String>> getCourseTags(List<Long> courseIds);

    List<CourseMySearchInfo> searchMyCourses(PaginationReq paginationReq, Long userId);

    PaginationRes countSearchMyCourses(PaginationReq paginationReq, Long userId);
}
