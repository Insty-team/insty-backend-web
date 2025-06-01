package insty.domain.course.persistence;

import static insty.model.course.QCourse.course;
import static insty.model.course.QCourseTag.courseTag;
import static insty.model.tag.QTags.tags;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import insty.domain.common.dto.PaginationReq;
import insty.domain.common.dto.PaginationRes;
import insty.domain.common.repository.QuerydslRepositorySupport;
import insty.domain.course.dto.CourseSearchFilter;
import insty.domain.course.dto.CourseSearchInfo;
import insty.domain.course.repository.CourseQueryRepository;
import insty.model.course.Course;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class CourseQueryRepositoryImpl extends QuerydslRepositorySupport implements CourseQueryRepository {

    protected CourseQueryRepositoryImpl() {
        super(Course.class);
    }

    @Override
    public List<CourseSearchInfo> searchCourses(PaginationReq paginationReq, CourseSearchFilter filter) {
        return select(
                Projections.constructor(
                        CourseSearchInfo.class,
                        course.id,
                        course.title,
                        course.description,
                        Expressions.nullExpression(List.class),
                        Expressions.nullExpression(String.class),
                        Expressions.nullExpression(String.class)
                )
        )
                .from(course)
                .where(searchFilter(filter.search()))
                .orderBy(createOrderSpecifier(null))
                .offset(paginationReq.getOffset())
                .limit(paginationReq.pageSize())
                .fetch();
    }

    @Override
    public PaginationRes countSearchCourses(PaginationReq paginationReq, CourseSearchFilter filter) {
        Long totalItems = select(course.count())
                .from(course)
                .where(searchFilter(filter.search()))
                .fetchOne();

        assert totalItems != null;
        return PaginationRes.of(totalItems.intValue(), paginationReq.page(), paginationReq.pageSize());
    }

    @Override
    public Map<Long, List<String>> getCourseTags(List<Long> courseIds) {
        return queryFactory()
                .select(course.id, tags.tagName)
                .from(course)
                .leftJoin(courseTag).on(courseTag.course.id.eq(course.id))
                .leftJoin(courseTag.tags, tags)
                .where(course.id.in(courseIds))
                .transform(GroupBy.groupBy(course.id).as(
                        GroupBy.list(tags.tagName)
                ));
    }

    /**
     * 필터
     */
    private BooleanExpression searchFilter(String search) {
        if (search == null || search.isEmpty()) {
            return null;
        }
        return course.title.contains(search);
    }
}
